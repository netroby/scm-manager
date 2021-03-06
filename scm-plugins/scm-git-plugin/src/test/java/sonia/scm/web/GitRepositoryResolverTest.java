/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.web;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;

/**
 * Unit tests for {@link GitRepositoryResolver}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class GitRepositoryResolverTest {
  
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  
  private File parentDirectory;
  
  @Mock
  private GitRepositoryHandler handler;
  
  @InjectMocks
  private GitRepositoryResolver resolver;
  
  @Before
  public void setUp() throws IOException {
    parentDirectory = temporaryFolder.newFolder();
    
    GitConfig config = new GitConfig();
    config.setRepositoryDirectory(parentDirectory);
    
    when(handler.getConfig()).thenReturn(config);
  }
  
  @Test
  public void testFindRepositoryWithoutDotGit() {
    createRepositories("a", "ab");
    
    File directory = resolver.findRepository(parentDirectory, "a");
    assertNotNull(directory);
    assertEquals("a", directory.getName());
    
    directory = resolver.findRepository(parentDirectory, "ab");
    assertNotNull(directory);
    assertEquals("ab", directory.getName());
  }
  
  @Test
  public void testFindRepositoryWithDotGit() {
    createRepositories("a", "ab");
    
    File directory = resolver.findRepository(parentDirectory, "a.git");
    assertNotNull(directory);
    assertEquals("a", directory.getName());
    
    directory = resolver.findRepository(parentDirectory, "ab.git");
    assertNotNull(directory);
    assertEquals("ab", directory.getName());
  }
  
  private void createRepositories(String... names) {
    for (String name : names) {
      assertTrue(new File(parentDirectory, name).mkdirs());
    }
  }
  
}
