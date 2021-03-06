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


package sonia.scm.url;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;
import sonia.scm.util.HttpUtil;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class RestModelUrlProviderTestBase
        extends ModelUrlProviderTestBase
{

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  protected abstract UrlProvider createUrlProvider(String baseUrl);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getExtension();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  @Override
  protected ModelUrlProvider createGroupModelUrlProvider(String baseUrl)
  {
    return createUrlProvider(baseUrl).getGroupUrlProvider();
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  @Override
  protected ModelUrlProvider createRepositoryModelUrlProvider(String baseUrl)
  {
    return createUrlProvider(baseUrl).getRepositoryUrlProvider();
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param urlPart
   *
   * @return
   */
  protected String createRestUrl(String baseUrl, String urlPart)
  {
    return createRestUrl(baseUrl, urlPart, getExtension());
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  @Override
  protected ModelUrlProvider createUserModelUrlProvider(String baseUrl)
  {
    return createUrlProvider(baseUrl).getUserUrlProvider();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param model
   *
   * @return
   */
  @Override
  protected String getExpectedAllUrl(String baseUrl, String model)
  {
    return createRestUrl(baseUrl, model);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param model
   * @param item
   *
   * @return
   */
  @Override
  protected String getExpectedDetailUrl(String baseUrl, String model,
          String item)
  {
    return createRestUrl(baseUrl,
                         model.concat(HttpUtil.SEPARATOR_PATH).concat(item));
  }

  @Test
  public void testGetDetailUrlWithSpaces()
  {
    String item = "Item with Spaces";

    for (String model : MODELS)
    {
      assertEquals(getExpectedDetailUrl(BASEURL, model, "Item%20with%20Spaces"),
        createModelUrlProvider(BASEURL, model).getDetailUrl(item));
    }
  }
}
