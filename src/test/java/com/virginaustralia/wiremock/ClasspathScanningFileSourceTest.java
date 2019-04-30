package com.virginaustralia.wiremock;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.TextFile;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ClasspathScanningFileSourceTest {

  @Test
  void itShouldReturnDefaults() {
    ClasspathScanningFileSource fileSource = new ClasspathScanningFileSource();
    assertTrue(fileSource.exists());
    assertEquals("__files", fileSource.getPath());
    assertEquals(URI.create("__files"), fileSource.getUri());
    assertEquals(fileSource, fileSource.child("something"));
    assertThrows(UnsupportedOperationException.class, fileSource::createIfNecessary);
    assertThrows(UnsupportedOperationException.class, () -> fileSource.deleteFile("any"));
    assertThrows(UnsupportedOperationException.class, () -> fileSource.writeTextFile("any", "something"));
    assertThrows(UnsupportedOperationException.class, () -> fileSource.writeBinaryFile("any", null));
  }

  @Test
  void itShouldLoadFileAtRoot() {
    ClasspathScanningFileSource fileSource = new ClasspathScanningFileSource();
    TextFile textFile = fileSource.getTextFileNamed("SOAP-fault.xml");
    assertTrue(textFile.readContentsAsString().contains("soap"));
  }

  @Test
  void itShouldLoadNestedFileTextContent() {
    ClasspathScanningFileSource fileSource = new ClasspathScanningFileSource();
    TextFile textFile = fileSource.getTextFileNamed("v1/version-1.xml");
    assertEquals("<version name=\"v1\"/>\n", textFile.readContentsAsString());
  }

  @Test
  void itShouldFailInvalidURL() throws URISyntaxException {
    ClassGraph graph = mock(ClassGraph.class);
    ScanResult scan = mock(ScanResult.class, Answers.RETURNS_DEEP_STUBS);
    given(graph.scan()).willReturn(scan);

    URL fakeURL = mock(URL.class);
    given(fakeURL.toString()).willReturn("fakeURL");
    given(fakeURL.toURI()).willThrow(new URISyntaxException("fake", "testing", 1));
    given(scan.getAllResources().getURLs()).willReturn(Collections.singletonList(fakeURL));
    ClasspathScanningFileSource fileSource = new ClasspathScanningFileSource(graph, ClasspathScanningFileSource.DEFAULT_PATH);

    assertThrows(RuntimeException.class, () -> fileSource.getTextFileNamed("fakeURL"));
    assertThrows(RuntimeException.class, () -> fileSource.getBinaryFileNamed("fakeURL"));

  }

  @Test
  void itShouldLoadNestedFileBinaryContent() {
    ClasspathScanningFileSource fileSource = new ClasspathScanningFileSource();
    BinaryFile textFile = fileSource.getBinaryFileNamed("v1/version-1.xml");
    assertEquals("<version name=\"v1\"/>\n", new String(textFile.readContents(), StandardCharsets.UTF_8));
  }

  @Test
  void itShouldFailLoadingNonExistingFiles() {
    ClasspathScanningFileSource fileSource = new ClasspathScanningFileSource();
    assertThrows(IllegalArgumentException.class, () -> fileSource.getTextFileNamed("no-there.file"));
  }

  @Test
  void itShouldListResources() {
    ClasspathScanningFileSource fileSource = new ClasspathScanningFileSource();
    List<TextFile> resources = fileSource.listFilesRecursively();
    assertFalse(resources.isEmpty());
  }

}
