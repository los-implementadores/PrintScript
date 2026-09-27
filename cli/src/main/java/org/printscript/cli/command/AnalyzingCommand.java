package org.printscript.cli.command;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.printscript.cli.io.Console;
import org.printscript.cli.io.FileAccess;
import org.printscript.common.configs.AnalyzerConfig;
import org.printscript.common.linterViolations.Violation;
import org.printscript.runner.AnalysisService;

public class AnalyzingCommand implements Command {

  private final AnalysisService service;
  private final Console console;
  private final FileAccess files;

  public AnalyzingCommand(AnalysisService service, Console console, FileAccess files) {
    this.service = service;
    this.console = console;
    this.files = files;
  }

  @Override
  public String name() {
    return "analyzing";
  }

  @Override
  public boolean acceptsConfig() {
    return true;
  }

  @Override
  public void execute(CommandRequest request) throws IOException {
    console.println("Analizando semantica y reglas estaticas (Linter)...");
    AnalyzerConfig config = loadConfig(request);

    List<Violation> violations;
    try (Reader source = files.openReader(request.filePath())) {
      violations = service.analyze(source, request.version(), config);
    }

    if (violations.isEmpty()) {
      console.println("Analyzing successful. (Tipos correctos y cero violaciones del Linter)");
      return;
    }
    console.println("Linter found violations:");
    for (Violation v : violations) {
      console.println(v.toString());
    }
  }

  private AnalyzerConfig loadConfig(CommandRequest request) throws IOException {
    if (request.configPath().isEmpty()) {
      return new AnalyzerConfig();
    }
    try (Reader reader = files.openReader(request.configPath().get())) {
      return AnalyzerConfig.fromJson(reader);
    }
  }
}
