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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.http.server.GitSmartHttpTools;

import sonia.scm.ClientMessages;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.web.filter.ProviderPermissionFilter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GitPermissionFilter extends ProviderPermissionFilter
{

  /** Field description */
  public static final String PARAMETER_SERVICE = "service";

  /** Field description */
  public static final String PARAMETER_VALUE_RECEIVE = "git-receive-pack";

  /** Field description */
  public static final String URI_RECEIVE_PACK = "git-receive-pack";

  /** Field description */
  public static final String URI_REF_INFO = "/info/refs";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param configuration
   * @param repositoryProvider
   */
  @Inject
  public GitPermissionFilter(ScmConfiguration configuration,
    RepositoryProvider repositoryProvider)
  {
    super(configuration, repositoryProvider);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   */
  @Override
  protected void sendNotEnoughPrivilegesError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    if (GitUtil.isGitClient(request))
    {
      GitSmartHttpTools.sendError(request, response,
        HttpServletResponse.SC_FORBIDDEN,
        ClientMessages.get(request).notEnoughPrivileges());
    }
    else
    {
      super.sendNotEnoughPrivilegesError(request, response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  protected boolean isWriteRequest(HttpServletRequest request) {

    String uri = request.getRequestURI();

    return uri.endsWith(URI_RECEIVE_PACK) ||
            (uri.endsWith(URI_REF_INFO) && PARAMETER_VALUE_RECEIVE.equals(request.getParameter(PARAMETER_SERVICE))) ||
            isLfsFileUpload(request);

  }

  @VisibleForTesting
  static boolean isLfsFileUpload(HttpServletRequest request) {
    String regex = String.format("^%s%s/.+(\\.git)?/info/lfs/objects/[a-z0-9]{64}$", request.getContextPath(), GitServletModule.GIT_PATH);
    return request.getRequestURI().matches(regex) && "PUT".equals(request.getMethod());

  }


}
