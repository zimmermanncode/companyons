package co.zimmermann.companyon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.tuple.Pair;

import com.vaadin.flow.component.ComponentEventListener;

import de.f0rce.ace.AceEditor;

import jep.Jep;
import jep.JepException;
import jep.SubInterpreter;


@Log4j2
public final class PythonThread extends Thread {
    @NonNull @Getter
    private final PythonConsole console;

    @NonNull
    private final AtomicReference<Jep> interpreter = new AtomicReference<>();

    @NonNull
    private final List<Pair<PythonInput, PythonCode>> execHistory = Collections.synchronizedList(new ArrayList<>());

    public PythonThread(@NonNull final PythonConsole console) {
        this.console = console;
    }

    @Override
    public void run() {
        @NonNull final Jep interpreter = new SubInterpreter();
        this.interpreter.set(interpreter);

        interpreter.exec("""
                from builtins import *

                # from com.vaadin.flow.component.html import *
                # from com.vaadin.flow.component.button import *
                """);

        interpreter.set("_UI_python_components", PythonComponent.COMPONENTS);
        interpreter.set("_UI_python_exec_history", this.execHistory);
        interpreter.set("_UI_python_view", this.console.getCompanyon().getView());

        interpreter.set("_UI_create_listener_wrapper", (Function<String, ComponentEventListener<?>>) (expression) -> (
                (event) -> {
                    execute(this.execHistory.get(0).getLeft(), new PythonCode(expression));
                }));

        interpreter.exec("""
                from companyons import UI

                UI = UI(locals())
                """);

        @Nullable AceEditor focusedPythonAce;
        @Nonnegative int aceCursorIndex = 0;

        @Nullable String completionSource = null;
        @NonNull String newCompletionSource;

        @Nullable Object completions = null;
        @Nullable Object newCompletions;

        @Nonnegative int historyIndex = 0;
        while (true) {
            // synchronized (this.execHistory) {

            focusedPythonAce = this.console.getFocusedPythonAce().orElse(null);
            if (focusedPythonAce == null) {
                completionSource = null;
                completions = null;

            } else {
                newCompletionSource = focusedPythonAce.getValue();
                aceCursorIndex = focusedPythonAce.getCursorPosition().getIndex();
                if (aceCursorIndex <= newCompletionSource.length()) {
                    newCompletionSource = newCompletionSource.substring(0, aceCursorIndex);
                }

                if (!newCompletionSource.equals(completionSource)) {
                    completionSource = newCompletionSource;
                    newCompletions = interpreter.invoke("UI.complete", completionSource);

                } else {
                    newCompletions = completions;
                }

                if (newCompletions instanceof List<?> completionList && !completionList.equals(completions)) {
                    completions = completionList;

                    @NonNull final var ace = focusedPythonAce;
                    ace.getUI().ifPresent(ui -> ui.access(() -> {
                        ace.addStaticWordCompleter(completionList.stream().map(Object::toString).toList(), false);
                    }));

                    log.info(completions.toString());
                }
            }

            while (this.execHistory.size() > historyIndex) {
                @NonNull final var outputLabel = String.valueOf(historyIndex);
                @NonNull final var historyEntry = this.execHistory.get(historyIndex++);

                // historyEntry.getLeft().getUI().ifPresent(ui -> ui.access(() -> {
                // @NonNull final Jep interpreter = new SubInterpreter();
                try {
                    // interpreter.set("UI._ui", historyEntry.getLeft().getUI().get());
                    interpreter.exec("UI.__class__._ui = UI.__class__._python_components['drawer'].getUI().get()");
                    // interpreter.exec(historyEntry.getRight());

                    historyEntry.getRight().evaluateWith(interpreter).ifPresent(result -> {
                        @NonNull final var resultText = result.toString();

                        this.console.getUI().ifPresent(ui -> ui.access(() -> {
                            this.console.addComponentAtIndex(this.console.indexOf(historyEntry.getLeft()) + 1,
                                    new PythonOutput(this.console, outputLabel, resultText));
                        }));
                    });

                } catch (final JepException e) {
                    log.error("Failed executing Python input ...", e);

                    this.console.getUI().ifPresent(ui -> ui.access(() -> {
                        this.console.addComponentAtIndex(this.console.indexOf(historyEntry.getLeft()) + 1,
                                new PythonErrorOutput(this.console, outputLabel, e));
                    }));
                }

                // interpreter.close();
                // }));
            }
            // }
        }
    }

    public void execute(@NonNull final PythonInput input, @NonNull final PythonCode code) {
        input.setLabel(String.valueOf(this.execHistory.size()));
        this.execHistory.add(Pair.of(input, code));
    }

    public void execute(@NonNull final PythonInput input, @NonNull final String expression) {
        this.execute(input, new PythonCode(expression));
    }

    public void launchKernel(@NonNull final PythonInput input) {
        this.execute(input, "__import__('ipykernel.kernelapp').kernelapp.launch_new_instance(user_ns=globals())");
    }
}
