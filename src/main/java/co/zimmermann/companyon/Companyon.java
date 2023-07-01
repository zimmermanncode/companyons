package co.zimmermann.companyon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import com.pivovarit.function.ThrowingBiConsumer;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.parser.Parser;

// import com.vaadin.flow.component.html.Div;
// import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;

/*
import com.storedobject.chart.BarChart;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
*/

@Log4j2
public class Companyon extends SplitLayout {

    public enum FileFormat {

        MARKDOWN(".md"),
        PYTHON(".py");

        @NonNull @Getter
        private final String extension;

        FileFormat(@NonNull final String extension) {
            this.extension = extension;
        }
    }

    @NonNull
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();

    @NonNull
    private static final Pattern SPECIAL_BLOCK_COMMENT_PATTERN = Pattern.compile(
            "(?m)# \\s*---\\s*([a-z]+)\\s*---\\s*\n((#( .*)?(\n|$))*)");

    @NonNull
    private static final Map<String, Function<String, AbstractBlock>> SPECIAL_BLOCK_CONSTRUCTORS = Map.of(
            "markdown", MarkdownCode::new,
            "python", PythonCode::new,
            "output", OutputBlock::new,
            "error", ErrorOutputBlock::new);

    @NonNull @Getter
    private final CompanyonTab drawerTab;

    @NonNull @Getter
    private final PythonConsole pythonConsole;

    @NonNull @Getter
    private final PythonComponent view;

    @NonNull
    public String getName() {
        return this.drawerTab.getName();
    }

    @NonNull @Getter @Setter
    private FileFormat fileFormat;

    public Companyon(
            @NonNull final String name,
            @NonNull final FileFormat fileFormat,
            @NonNull final List<AbstractBlock> inputScript) {

        super(Orientation.HORIZONTAL);
        super.setSizeFull();

        this.fileFormat = fileFormat;

        this.view = new PythonComponent(name);
        // super.addToPrimary(new Scroller(new Div(this.view), Scroller.ScrollDirection.BOTH));
        super.addToPrimary(this.view);

        /*
        @NonNull final var coords = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
        @NonNull final var bars = new BarChart(new CategoryData(" ", "D", "E", "H", "L", "O", "R", "W"), new Data(1, 1, 1, 1, 3, 2, 1, 1));
        bars.plotOn(coords);
        bars.setStackName("Letter count");

        @NonNull final var chart = new SOChart();
        chart.add(coords);
        chart.setSizeFull();

        this.view.add(chart);
        */

        this.pythonConsole = new PythonConsole(this, inputScript);
        // super.addToSecondary(new Scroller(new Div(this.pythonConsole), Scroller.ScrollDirection.BOTH));
        super.addToSecondary(this.pythonConsole);

        this.drawerTab = new CompanyonTab(this, name);
    }

    public Companyon() {
        this("New Companyon", FileFormat.MARKDOWN, List.of());
    }

    public void saveToMarkdown(@NonNull final File file) {
        try (@NonNull final var fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {

            @NonNull final ThrowingBiConsumer<String, AbstractInputOutput, IOException>
                    fencedCodeBlockWriter = (info, intputOutput) -> {
                        fileWriter.write("```");
                        fileWriter.write(info);
                        fileWriter.write("\n");
                        fileWriter.write(intputOutput.toString().stripTrailing());
                        fileWriter.write("\n```\n\n");
                    };

            for (@NonNull final var inputOutput : this.pythonConsole.getInputsOutputs()) {
                if (inputOutput instanceof MarkdownInput markdownInput) {
                    fileWriter.write(markdownInput.toString().stripTrailing());
                    fileWriter.write("\n\n");

                } else if (inputOutput instanceof MarkdownOutput markdownOutput) {
                    fileWriter.write(markdownOutput.toString().stripTrailing());
                    fileWriter.write("\n\n");

                } else if (inputOutput instanceof PythonInput pythonInput) {
                    fencedCodeBlockWriter.accept("python", pythonInput);

                } else if (inputOutput instanceof  PythonErrorOutput pythonError) {
                    fencedCodeBlockWriter.accept("error", pythonError);

                } else if (inputOutput instanceof  PythonOutput pythonOutput) {
                    fencedCodeBlockWriter.accept("output", pythonOutput);
                }
            }

        } catch (final IOException e) {
            log.error("Failed saving '{}'", file.getName(), e);
        }
    }

    public void saveToPython(@NonNull final File file) {
        try (@NonNull final var fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {

            @NonNull final ThrowingBiConsumer<String, String, IOException> commentBlockWriter = (info, text) -> {
                fileWriter.write("# --- ");
                fileWriter.write(info);
                fileWriter.write(" ---\n");

                for (@NonNull final var line : text.stripTrailing().split("(?m)\\s*\n")) {
                    fileWriter.write("#");
                    if (line.length() > 0) {
                        fileWriter.write(' ');
                        fileWriter.write(line);
                    }

                    fileWriter.write("\n");
                }
            };

            for (@NonNull final var inputOutput : this.pythonConsole.getInputsOutputs()) {
                if (inputOutput instanceof PythonInput pythonInput) {
                    fileWriter.write(pythonInput.toString().stripTrailing());
                    fileWriter.write("\n\n\n");

                } else if (inputOutput instanceof PythonErrorOutput pythonError) {
                    commentBlockWriter.accept("error", pythonError.toString());
                    fileWriter.write("\n\n");

                } else if (inputOutput instanceof PythonOutput pythonOutput) {
                    commentBlockWriter.accept("output", pythonOutput.toString());
                    fileWriter.write("\n\n");

                } else if (inputOutput instanceof MarkdownInput markdownInput) {
                    commentBlockWriter.accept("markdown", markdownInput.toString());
                    fileWriter.write("\n\n");

                } else if (inputOutput instanceof MarkdownOutput markdownOutput) {
                    commentBlockWriter.accept("markdown", markdownOutput.toString());
                    fileWriter.write("\n\n");
                }
            }

        } catch (final IOException e) {
            log.error("Failed saving Companyon to '{}' ...", file.getName(), e);
        }
    }

    public void save() {
        switch (this.fileFormat) {
            case MARKDOWN -> {
                this.saveToMarkdown(new File(this.getName() + ".md"));
            }

            case PYTHON -> {
                this.saveToPython(new File(this.getName() + ".py"));
            }
        }
    }

    public void saveTo(@NonNull final FileFormat fileFormat) {
        this.fileFormat = fileFormat;
        this.save();
    }

    @NonNull
    public static Optional<Companyon> loadFromMarkdown(@NonNull final File companyonFile) {
        @NonNull final String markdownText;
        try {
            markdownText = FileUtils.readFileToString(companyonFile, StandardCharsets.UTF_8);

        } catch (final IOException e) {
            log.error("Failed reading Companyon from '{}' ...", companyonFile, e);
            return Optional.empty();
        }

        @NonNull final var markdownDocument = MARKDOWN_PARSER.parse(markdownText);

        @NonNull final var inputScript = new ArrayList<AbstractBlock>();
        for (@NonNull final var markdownNode : markdownDocument.getChildren()) {
            if (markdownNode instanceof FencedCodeBlock block) {
                @NonNull final var blockInfo = block.getInfo().toString();

                @Nullable final var blockConstructor = SPECIAL_BLOCK_CONSTRUCTORS.get(blockInfo);
                if (blockConstructor != null) {
                    @NonNull final var blockTextWriter = new StringWriter();
                    for (@NonNull final var line : block.getContentLines()) {
                        blockTextWriter.write(line.toString());
                    }

                    inputScript.add(blockConstructor.apply(blockTextWriter.toString()));
                    continue;
                }
            }

            inputScript.add(new MarkdownCode(markdownNode));
        }

        return Optional.of(new Companyon(FileNameUtils.getBaseName(companyonFile.getName()), FileFormat.MARKDOWN,
                inputScript));
    }

    @NonNull
    public static Optional<Companyon> loadFromPython(@NonNull final File companyonFile) {
        @NonNull final String pythonText;
        try {
            pythonText = FileUtils.readFileToString(companyonFile, StandardCharsets.UTF_8);

        } catch (final IOException e) {
            log.error("Failed reading Companyon from '{}' ...", companyonFile, e);
            return Optional.empty();
        }

        @NonNull final var inputScript = new ArrayList<AbstractBlock>();
        for (@NonNull final var blockText : pythonText.split("(?m)\\r?\n\\r?\n\\r?\n")) {

            @NonNull final var matcher = SPECIAL_BLOCK_COMMENT_PATTERN.matcher(blockText);
            if (matcher.matches()) {
                @NonNull final var blockInfo = matcher.group(1);

                @Nullable final var blockConstructor = SPECIAL_BLOCK_CONSTRUCTORS.get(blockInfo);
                if (blockConstructor != null) {
                    @NonNull final var specialBlockText = matcher.group(2).replaceAll("(?m)^# ", "");
                    inputScript.add(blockConstructor.apply(specialBlockText));
                    continue;
                }
            }

            inputScript.add(new PythonCode(blockText));
        }

        return Optional.of(new Companyon(FileNameUtils.getBaseName(companyonFile.getName()), FileFormat.PYTHON,
                inputScript));
    }
}
