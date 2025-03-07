package io.github.vickycmd.config.parser.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class DocGenArguments {
    private final String sourceDir;
    private final String sourceSubDir;
    private final String outputDir;
    private String applicationName;
}
