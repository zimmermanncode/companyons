package co.zimmermann.companyon;

import java.io.File;

import javax.annotation.Nullable;

import lombok.NonNull;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tabs;

import com.vaadin.flow.router.PageTitle;


@CssImport("./styles/companyons.css")
@PageTitle("Companyons")
public class MainLayout extends AppLayout {

    @NonNull
    private final Tabs companyonTabs = new Tabs();

    public MainLayout() {
        super.setPrimarySection(AppLayout.Section.DRAWER);

        @NonNull final var mainTitle = new H3("Companyons");
        mainTitle.getStyle().set("margin", "0.5em");

        @NonNull final var drawerTitle = new HorizontalLayout(mainTitle);
        drawerTitle.setAlignItems(FlexComponent.Alignment.BASELINE);
        drawerTitle.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        drawerTitle.setWidthFull();

        @NonNull final var headerTitle = new H3();
        headerTitle.getStyle().set("margin", "0.5em");

        @NonNull final var saveSelect = new Select<Companyon.FileFormat>();
        saveSelect.setId("companyons-save-select");
        saveSelect.setItemLabelGenerator(Companyon.FileFormat::getExtension);
        saveSelect.setItems(Companyon.FileFormat.MARKDOWN, Companyon.FileFormat.PYTHON);

        saveSelect.addAttachListener(event -> {
            event.getUI().getPage().executeJs(
                    "document.querySelector('#companyons-save-select').shadowRoot.querySelector('.vaadin-select-container')"
                            + ".style.width = '8em'");
        });

        this.companyonTabs.setOrientation(Tabs.Orientation.VERTICAL);
        this.companyonTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() instanceof Companyon.Tab companyonTab) {
                @NonNull final var companyon = companyonTab.getCompanyon();

                super.getUI().ifPresent(ui -> ui.access(() -> {
                    super.setContent(companyon);
                    headerTitle.setText(companyon.getName());
                    saveSelect.setValue(companyon.getFileFormat());
                }));
            }
        });

        @NonNull final var plusButton = new Button(VaadinIcon.PLUS.create(), ignoredEvent -> {
            this.addCompanyon(new Companyon());
        });

        this.addAttachListener(ignoredEvent -> {
            @NonNull final var files = new File(".").listFiles();
            if (files != null) {
                for (@NonNull final var file : files) {
                    @NonNull final var fileName = file.getName();

                    if (fileName.endsWith(Companyon.FileFormat.MARKDOWN.getExtension())) {
                        this.loadCompanyonFromMarkdown(file);
                    }

                    if (fileName.endsWith(Companyon.FileFormat.PYTHON.getExtension())) {
                        this.loadCompanyonFromPython(file);
                    }
                }
            }
        });

        saveSelect.addValueChangeListener(event -> {
            if (this.getContent() instanceof Companyon companyon) {
                companyon.setFileFormat(event.getValue());
            }
        });

        @NonNull final var saveButton = new Button(VaadinIcon.DISC.create(), ignoredEvent -> {
            if (this.getContent() instanceof Companyon companyon) {
                companyon.save();
            }
        });

        // saveButton.setIconAfterText(false);
        saveSelect.setPrefixComponent(saveButton);

        @NonNull final var renameButton = new Button(VaadinIcon.PENCIL.create(), ignoredEvent -> {
            if (this.getContent() instanceof Companyon companyon) {
                companyon.getDrawerTab().editName();
            }
        });

        @NonNull final var toolBar = new HorizontalLayout(plusButton, saveSelect, renameButton);
        toolBar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolBar.setWidthFull();

        super.addToDrawer(drawerTitle, toolBar, this.companyonTabs, new PythonComponent("drawer"));

        @NonNull final var playAllButton = new Button(VaadinIcon.PLAY.create(), ignoredEvent -> {
            if (super.getContent() instanceof Companyon companyon) {
                companyon.getPythonConsole().executeAll();
            }
        });

        playAllButton.getStyle().set("margin-right", "1em");

        @NonNull final var headerBar = new HorizontalLayout(headerTitle, playAllButton);
        headerBar.setAlignItems(FlexComponent.Alignment.BASELINE);
        headerBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerBar.setWidthFull();

        @NonNull final var header = new Header(headerBar);
        header.getStyle().set("display", "flex");
        header.setWidthFull();

        super.addToNavbar(header);

        @NonNull final var drawerToggle = new DrawerToggle();
        @NonNull final Runnable drawerToggleAdder = () -> {
            if (super.isDrawerOpened()) {
                drawerTitle.add(drawerToggle);

            } else {
                header.addComponentAsFirst(drawerToggle);
            }
        };

        drawerToggleAdder.run();
        drawerToggle.addClickListener(ignoredEvent -> {
            drawerToggleAdder.run();
        });

        super.addAttachListener(ignoredEvent -> {
            super.getUI().ifPresent(ui -> ui.getPage().addBrowserWindowResizeListener(ignoredResizeEvent -> {
                drawerToggleAdder.run();
            }));
        });
    }

    @NonNull
    public void addCompanyon(@NonNull final Companyon companyon) {
        @NonNull final var companyonTab = companyon.getDrawerTab();

        super.getUI().ifPresent(ui -> ui.access(() -> {
            if (!this.companyonTabs.getChildren().toList().contains(companyonTab)) {
                this.companyonTabs.add(companyonTab);
                companyonTab.setSelected(true);
            }
        }));
    }

    @NonNull
    public void loadCompanyonFromMarkdown(@NonNull final File companyonFile) {
        Companyon.loadFromMarkdown(companyonFile).ifPresent(companyon -> {
            this.addCompanyon(companyon);
        });
    }

    @NonNull
    public void loadCompanyonFromPython(@NonNull final File companyonFile) {
        Companyon.loadFromPython(companyonFile).ifPresent(companyon -> {
            this.addCompanyon(companyon);
        });
    }
}
