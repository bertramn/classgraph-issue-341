package com.virginaustralia.wiremock;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClasspathScanningFileSource implements FileSource {

  public static final String UNSUPPORTED_ERROR = "this is a readonly file system";

  public static final String DEFAULT_PATH = "__files";

  private final String path;

  private final ClassGraph graph;

  private final List<URL> resources = new ArrayList<>();

  private boolean initialised = false;

  public ClasspathScanningFileSource() {
    this(DEFAULT_PATH);
  }

  public ClasspathScanningFileSource(String path) {
    this(new ClassGraph().whitelistPaths(path), path);
  }

  public ClasspathScanningFileSource(ClassGraph graph, String path) {
    this.graph = graph;
    this.path = path;
  }

  public List<URL> getScannedResources() {
    if (!initialised) {
      try (ScanResult scanResult = graph.scan()) {
        resources.addAll(scanResult.getAllResources().getURLs());
      } finally {
        initialised = true;
      }
    }
    return resources;
  }

  @Override
  public List<TextFile> listFilesRecursively() {
    return getScannedResources().stream()
      .map(ClasspathScanningFileSource::textFile)
      .collect(Collectors.toList());
  }

  @Override
  public TextFile getTextFileNamed(String name) {
    return getScannedResources().stream()
      .filter(r -> r.toString().endsWith(name))
      .map(ClasspathScanningFileSource::textFile)
      .findAny()
      .orElseThrow(() -> new IllegalArgumentException("file with name " + name + " does not exist"));
  }

  @Override
  public BinaryFile getBinaryFileNamed(String name) {
    return getScannedResources().stream()
      .filter(r -> r.toString().endsWith(name))
      .map(ClasspathScanningFileSource::binaryFile)
      .findAny()
      .orElseThrow(() -> new IllegalArgumentException("file with name " + name + " does not exist"));
  }

  // region helpers

  private static TextFile textFile(@NotNull URL url) {
    try {
      return new TextFile(url.toURI());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("failed to create text file for " + url.toString(), e);
    }
  }

  private static BinaryFile binaryFile(@NotNull URL url) {
    try {
      return new BinaryFile(url.toURI());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("failed to create text file for " + url.toString(), e);
    }
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public URI getUri() {
    return URI.create(path);
  }

  // endregion

  // region unsupported

  @Override
  public FileSource child(String subDirectoryName) {
    return this;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public void createIfNecessary() {
    throw new UnsupportedOperationException(UNSUPPORTED_ERROR);
  }

  @Override
  public void writeTextFile(String name, String contents) {
    throw new UnsupportedOperationException(UNSUPPORTED_ERROR);
  }

  @Override
  public void writeBinaryFile(String name, byte[] contents) {
    throw new UnsupportedOperationException(UNSUPPORTED_ERROR);
  }

  @Override
  public void deleteFile(String name) {
    throw new UnsupportedOperationException(UNSUPPORTED_ERROR);
  }

  // endregion

}
