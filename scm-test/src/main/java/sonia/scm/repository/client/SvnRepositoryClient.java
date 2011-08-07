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



package sonia.scm.repository.client;

//~--- non-JDK imports --------------------------------------------------------

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnRepositoryClient extends AbstractRepositoryClient
{

  /**
   * Constructs ...
   *
   *
   * @param localRepository
   * @param remoteRepository
   * @param username
   * @param password
   *
   * @throws SVNException
   */
  SvnRepositoryClient(File localRepository, String remoteRepository,
                      String username, String password)
          throws SVNException
  {
    super(localRepository, remoteRepository);

    DefaultSVNOptions options = new DefaultSVNOptions();

    if ((username != null) && (password != null))
    {
      client = SVNClientManager.newInstance(options, username, password);
    }
    else
    {
      client = SVNClientManager.newInstance(options);
    }

    remoteRepositoryURL = SVNURL.parseURIDecoded(remoteRepository);
    DAVRepositoryFactory.setup();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   * @param others
   *
   * @throws RepositoryClientException
   */
  @Override
  public void add(String file, String... others)
          throws RepositoryClientException
  {
    files.add(file);

    if (others != null)
    {
      files.addAll(Arrays.asList(others));
    }
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryClientException
   */
  @Override
  public void checkout() throws RepositoryClientException
  {
    try
    {
      SVNUpdateClient updateClient = client.getUpdateClient();

      updateClient.doCheckout(remoteRepositoryURL, localRepository,
                              SVNRevision.UNDEFINED, SVNRevision.HEAD,
                              SVNDepth.FILES, false);
    }
    catch (SVNException ex)
    {
      throw new RepositoryClientException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param message
   *
   * @throws RepositoryClientException
   */
  @Override
  public void commit(String message) throws RepositoryClientException
  {
    checkout();

    SVNCommitClient cc = client.getCommitClient();

    try
    {
      for (String name : files)
      {
        File file = new File(localRepository, name);

        cc.doImport(file, remoteRepositoryURL.appendPath(name, true), message,
                    null, false, true, SVNDepth.FILES);
      }
    }
    catch (SVNException ex)
    {
      throw new RepositoryClientException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryClientException
   */
  @Override
  public void init() throws RepositoryClientException
  {

    // do nothing
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SVNClientManager client;

  /** Field description */
  private List<String> files = new ArrayList<String>();

  /** Field description */
  private SVNURL remoteRepositoryURL;
}
