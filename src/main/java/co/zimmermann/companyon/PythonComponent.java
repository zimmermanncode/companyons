package co.zimmermann.companyon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;


/**
  * A {@link VerticalLayout} whose content is defined via Python.
  */
@Log4j2
public class PythonComponent extends VerticalLayout {

    @NonNull
    public static final Map<String, PythonComponent> COMPONENTS = Collections.synchronizedMap(new HashMap<>());

    public PythonComponent(@NonNull final String name) {
        COMPONENTS.put(name, this);

        /*
        this.add(new Button("Python …", event -> {
            // THREAD.execute(this, String.format("COMPONENTS['%s'].add(H1('title'), H2('subtitle'))", name));
            THREAD.launchKernel(this);
        }));
        */
    }
}
