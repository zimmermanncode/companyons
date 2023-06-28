package co.zimmermann.companyon;

import javax.annotation.Nullable;

import lombok.NonNull;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.vaadin.flow.dom.DomEventListener;


// @JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public abstract class AbstractInputOutput extends HorizontalLayout {

    @NonNull
    protected final PythonConsole console;

    @NonNull
    protected final Details details = new Details();

    @NonNull
    protected final H5 badge = new H5();

    @NonNull
    protected final HorizontalLayout toolbar = new HorizontalLayout();

    protected AbstractInputOutput(
            @NonNull final PythonConsole console,
            @Nullable final String badgeVariant,
            @Nullable final String label) {

        this.console = console;

        // this.badge.getElement().getThemeList().add("badge " + Optional.ofNullable(badgeVariant).orElse(""));
        if (badgeVariant != null) {
            this.badge.getStyle().set("color", String.format("var(--lumo-%s-text-color)", badgeVariant));
        }

        if (label != null) {
            this.badge.setText(String.format("[ %s ]", label));
        }

        // this.toolbar.add(this.badge);
        // this.toolbar.setAlignItems(Alignment.BASELINE);
        this.toolbar.setPadding(false);
        this.toolbar.setSpacing(false);
        this.toolbar.setWidth("");

        @NonNull final var summary = new HorizontalLayout(this.badge, toolbar);
        summary.setAlignItems(Alignment.BASELINE);
        summary.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        summary.setPadding(false);
        summary.setSpacing(false);
        summary.setWidthFull();

        this.details.addThemeVariants(DetailsVariant.FILLED, DetailsVariant.SMALL);
        this.details.setOpened(true);
        this.details.setSummary(summary);
        this.details.setWidthFull();

        super.add(details);
        // super.add(ace);

        super.setPadding(false);
        super.setSpacing(false);
        super.setWidthFull();
    }

    public void setLabel(@NonNull final String text) {
        super.getUI().ifPresent(ui -> ui.access(() -> {
            this.badge.setText(String.format("[ %s ]", text));
        }));
    }

    protected void addToolbarButton(@NonNull final VaadinIcon icon, @NonNull final DomEventListener clickEventListener) {
        @NonNull final var button = new Button(icon.create());
        button.getElement().addEventListener("click", clickEventListener).addEventData("event.stopPropagation()");
        this.toolbar.add(button);
    }

    public abstract void delete();
}
