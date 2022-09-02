package co.zimmermann.companyon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.annotation.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.tuple.Pair;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Node;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import jep.Jep;
import jep.JepException;
import jep.SubInterpreter;


public class PythonConsole extends VerticalLayout {

    public static class Code {

        @NonNull
        private final String text;

        public Code(@NonNull final String text) {
            this.text = text;
        }

        @NonNull @Override
        public String toString() {
            return this.text;
        }
    }

    public static final class Markdown extends Code {

        @NonNull
        private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

        @NonNull
        private final Node node;

        public Markdown(@NonNull final Node node) {
            super(node.toString());
            this.node = node;
        }

        @NonNull
        public Html toHtml() {
            return new Html(HTML_RENDERER.render(this.node));
        }
    }

    @Log4j2
    public static final class Python extends Thread {
        @NonNull
        private final PythonConsole console;

        @NonNull
        private final AtomicReference<Jep> interpreter = new AtomicReference<>();

        @NonNull
        private final List<Pair<PythonInput, String>> execHistory = Collections.synchronizedList(new ArrayList<>());

        public Python(@NonNull final PythonConsole console) {
            this.console = console;
        }

        @Override
        public void run() {
            @NonNull final Jep interpreter = new SubInterpreter();
            this.interpreter.set(interpreter);

            interpreter.exec("""
                    from builtins import *

                    from com.vaadin.flow.component.html import *
                    from com.vaadin.flow.component.button import *

                    class UI:
                        # from com.vaadin.flow.component.html import *
                        # from com.vaadin.flow.component.button import *

                        @staticmethod
                        def accessor(func: __import__('types').FunctionType) -> __import__('types').FunctionType:
                            def wrapper(*args, **kwargs):
                                return UI._ui.accessSynchronously(lambda: func(*args, **kwargs))

                            return wrapper

                        _listeners: __import__('typing').List[__import__('types').FunctionType] = []

                        @staticmethod
                        def listener(func: __import__('types').FunctionType) -> __import__('types').FunctionType:
                            # def wrapper(event):
                            #     UI._listeners.append(__import__('functools').partial(func, event))
                            #     UI._python_exec_history.add(f"UI._listeners[{len(UI._listeners) - 1}]()")

                            # return wrapper

                            UI._listeners.append(func)
                            return UI._create_listener_wrapper.apply(f"UI._listeners[{len(UI._listeners) - 1}]()")

                        def __getitem__(self, python_component_name):
                            return UI._python_components[python_component_name]
                    """);

            interpreter.set("_UI_python_components", PythonComponent.COMPONENTS);
            interpreter.set("_UI_python_exec_history", this.execHistory);
            interpreter.set("_UI_python_view", this.console.getCompanyon().getView());

            interpreter.set("_UI_create_listener_wrapper", (Function<String, ComponentEventListener<?>>) (pythonCode) -> (
                    (event) -> {
                        execute(this.execHistory.get(0).getLeft(), pythonCode);
                    }));

            interpreter.exec("""
                    UI._python_components = globals().pop('_UI_python_components')
                    UI._python_exec_history = globals().pop('_UI_python_exec_history')
                    UI.view = globals().pop('_UI_python_view')

                    UI._create_listener_wrapper = globals().pop('_UI_create_listener_wrapper')

                    UI = UI()
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
                            interpreter.exec(historyEntry.getRight());

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

        public void execute(@NonNull final PythonInput input, @NonNull final String expression) {
            this.execHistory.add(Pair.of(input, expression));
        }

        public void launchKernel(@NonNull final PythonInput input) {
            this.execute(input, "__import__('ipykernel.kernelapp').kernelapp.launch_new_instance(user_ns=globals())");
        }
    }

    @NonNull @Getter
    private final Companyon companyon;

    @NonNull
    private final Python python;

    @NonNull
    private final Button plusButton = new Button(VaadinIcon.PLUS.create());

    @NonNull
    public List<PythonInput> getInputs() {
        return this.getChildren().filter(PythonInput.class::isInstance).map(PythonInput.class::cast).toList();
    }

    public PythonConsole(@NonNull final Companyon companyon, @NonNull final List<Code> inputScript) {
        this.companyon = companyon;

        @NonNull final var python = this.python = new Python(this);
        python.start();

        if (inputScript.isEmpty()) {
            super.add(new PythonInput(python, null));

        } else {
            for (@NonNull final var code : inputScript) {
                if (code instanceof Markdown markdown) {
                    super.add(markdown.toHtml());

                } else {
                    super.add(new PythonInput(python, code));
                }
            }
        }

        this.plusButton.addClickListener(ignoredEvent -> {
            this.addPythonInput(null);
        });

        super.add(plusButton);
    }

    public void addPythonInput(@Nullable final Code code) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            super.addComponentAtIndex(super.indexOf(this.plusButton), new PythonInput(this.python, code));
        }));
    }

    public void executeAll() {
        this.getInputs().forEach(pythonInput -> {
            pythonInput.execute();
        });
    }
}
