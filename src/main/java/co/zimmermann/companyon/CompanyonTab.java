package co.zimmermann.companyon;

import lombok.Getter;
import lombok.NonNull;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;


public class CompanyonTab extends com.vaadin.flow.component.tabs.Tab {

    @NonNull @Getter
    private final Companyon companyon;

    @NonNull
    private final TextField nameField = new TextField();

    @NonNull
    public String getName() {
        return this.nameField.getValue();
    }

    CompanyonTab(@NonNull final Companyon companyon, @NonNull final String name) {
        this.companyon = companyon;

        this.nameField.setPattern("[^/\\\\;:]+");
        this.nameField.setReadOnly(true);
        this.nameField.setRequired(true);

        this.nameField.setValue(name);
        this.nameField.setValueChangeMode(ValueChangeMode.ON_BLUR);
        this.nameField.addBlurListener(ignoredEvent -> {
            this.nameField.setReadOnly(true);
        });

        this.nameField.addKeyDownListener(event -> {
            @NonNull final var key = event.getKey();

            if (key.equals(Key.ENTER) || key.equals(Key.of("Escape"))) {
                this.nameField.blur();
            }
        });

        super.add(this.nameField);
    }

    public void editName() {
        this.nameField.setReadOnly(false);
        this.nameField.focus();
    }
}
