package co.zimmermann.companyon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.storedobject.chart.BarChart;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.parser.Parser;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.value.ValueChangeMode;


@Log4j2
public class Companyon extends SplitLayout {

    public static class Tab extends com.vaadin.flow.component.tabs.Tab {

        @NonNull @Getter
        private final Companyon companyon;

        @NonNull
        private final TextField nameField = new TextField();

        @NonNull
        public String getName() {
            return this.nameField.getValue();
        }
        private Tab(@NonNull final Companyon companyon, @NonNull final String name) {
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

            super.add(nameField);
        }

        public void editName() {
            this.nameField.setReadOnly(false);
            this.nameField.focus();
        }
    }

    @NonNull @Getter
    private final Tab drawerTab;

    @NonNull @Getter
    private final PythonConsole pythonConsole;

    @NonNull @Getter
    private final PythonComponent view;

    @NonNull
    public String getName() {
        return this.drawerTab.getName();
    }

    public Companyon(@NonNull final String name, @NonNull final List<PythonConsole.Code> inputScript) {
        super(Orientation.HORIZONTAL);

        this.view = new PythonComponent(name);
        super.addToPrimary(this.view);

        @NonNull final var coords = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
        @NonNull final var bars = new BarChart(new CategoryData(" ", "D", "E", "H", "L", "O", "R", "W"), new Data(1, 1, 1, 1, 3, 2, 1, 1));
        bars.plotOn(coords);
        bars.setStackName("Letter count");

        @NonNull final var chart = new SOChart();
        chart.add(coords);
        chart.setSizeFull();

        this.view.add(chart);

        this.pythonConsole = new PythonConsole(this, inputScript);
        super.addToSecondary(this.pythonConsole);

        this.drawerTab = new Tab(this, name);
    }

    public Companyon() {
        this("New Companyon", List.of());
    }

    public void saveTo(@NonNull final File file) {
        try (@NonNull final var fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {

            for (@NonNull final var pythonInput : this.pythonConsole.getInputs()) {
                fileWriter.write("```python\n");
                fileWriter.write(pythonInput.getCode());
                fileWriter.write("\n```\n\n");
            }

        } catch (final IOException e) {
            log.error("Failed saving '{}'", file.getName(), e);
        }
    }

    public void save() {
        this.saveTo(new File(this.getName() + ".md"));
    }

    @NonNull
    public static Optional<Companyon> loadFrom(@NonNull final File companyonFile) {
        @NonNull final String markdownText;
        try {
            markdownText = FileUtils.readFileToString(companyonFile, StandardCharsets.UTF_8);

        } catch (final IOException e) {
            log.error("Failed reading '{}'", companyonFile, e);
            return Optional.empty();
        }

        @NonNull final var markdownParser = Parser.builder().build();
        @NonNull final var markdown = markdownParser.parse(markdownText);

        @NonNull final var inputScript = new ArrayList<PythonConsole.Code>();
        markdown.getChildren().forEach(markdownNode -> {
            if (markdownNode instanceof FencedCodeBlock codeBlock && codeBlock.getInfo().equals("python")) {

                @NonNull final var pythonCodeWriter = new StringWriter();
                codeBlock.getContentLines().forEach(codeLine -> {
                    pythonCodeWriter.write(codeLine.toString());
                });

                inputScript.add(new PythonConsole.Code(pythonCodeWriter.toString()));

            } else {
                inputScript.add(new PythonConsole.Markdown(markdownNode));
            }
        });

        return Optional.of(new Companyon(FileNameUtils.getBaseName(companyonFile.getName()), inputScript));
    }
}
