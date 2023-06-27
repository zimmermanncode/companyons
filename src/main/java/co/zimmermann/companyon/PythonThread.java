package co.zimmermann.companyon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.tuple.Pair;

import com.vaadin.flow.component.ComponentEventListener;

import jep.Jep;
import jep.JepException;
import jep.SubInterpreter;


@Log4j2
public final class PythonThread extends Thread {
    @NonNull
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

                UI = UI(globals())
                """);

        int historyIndex = 0;
        while (true) {
            // synchronized (this.execHistory) {
            while (this.execHistory.size() > historyIndex) {
                @NonNull final var historyEntry = this.execHistory.get(historyIndex++);

                // historyEntry.getLeft().getUI().ifPresent(ui -> ui.access(() -> {
                // @NonNull final Jep interpreter = new SubInterpreter();
                try {
                    // interpreter.set("UI._ui", historyEntry.getLeft().getUI().get());
                    interpreter.exec("UI.__class__._ui = UI.__class__._python_components['drawer'].getUI().get()");
                    // interpreter.exec(historyEntry.getRight());

                    historyEntry.getRight().evaluateWith(interpreter).ifPresent(result -> {
                        this.console.getUI().ifPresent(ui -> ui.access(() -> {
                            this.console.addComponentAtIndex(this.console.indexOf(historyEntry.getLeft()) + 1,
                                    new PythonOutput(this.console, result.toString()));
                        }));
                    });

                } catch (final JepException e) {
                    log.error("Failed executing Python input", e);

                    this.console.getUI().ifPresent(ui -> ui.access(() -> {
                        this.console.addComponentAtIndex(this.console.indexOf(historyEntry.getLeft()) + 1,
                                new PythonOutput(this.console, e.getMessage()));
                    }));
                }
                // interpreter.close();
                // }));
            }
            // }
        }
    }

    public void execute(@NonNull final PythonInput input, @NonNull final PythonCode code) {
        this.execHistory.add(Pair.of(input, code));
    }

    public void execute(@NonNull final PythonInput input, @NonNull final String expression) {
        this.execHistory.add(Pair.of(input, new PythonCode(expression)));
    }

    public void launchKernel(@NonNull final PythonInput input) {
        this.execute(input, "__import__('ipykernel.kernelapp').kernelapp.launch_new_instance(user_ns=globals())");
    }
}
