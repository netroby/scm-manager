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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;
import java.net.URLEncoder;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Util method for the http protocol.
 *
 * @author Sebastian Sdorra
 */
public final class HttpUtil
{

  /** authentication realm for basic authentication */
  public static final String AUTHENTICATION_REALM = "SONIA :: SCM Manager";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /**
   * content-length header
   * @since 1.46
   */
  public static final String HEADER_CONTENT_LENGTH = "Content-Length";

  /**
   * location header
   * @since 1.43
   */
  public static final String HEADER_LOCATION = "Location";

  /**
   * header for identifying the scm-manager client
   * @since 1.19
   */
  public static final String HEADER_SCM_CLIENT = "X-SCM-Client";

  /** Field description */
  public static final String HEADER_USERAGENT = "User-Agent";

  /** authentication header */
  public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

  /**
   * The original host requested by the client in the Host HTTP request header.
   * @since 1.47
   */
  public static final String HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";

  /**
   * The original port requested by the client.
   * @since 1.47
   */
  public static final String HEADER_X_FORWARDED_PORT = "X-Forwarded-Port";

  /**
   * The original protocol (http or https) requested by the client.
   * @since 1.47
   */
  public static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";

  /**
   * Default http port
   * @since 1.5
   */
  public static final int PORT_HTTP = 80;

  /**
   * Default https port
   * @since 1.5
   */
  public static final int PORT_HTTPS = 443;

  /**
   * Default http scheme
   * @since 1.5
   */
  public static final String SCHEME_HTTP = "http";

  /**
   * Default https scheme
   * @since 1.5
   */
  public static final String SCHEME_HTTPS = "https";

  /**
   * Possible value of the X-SCM-Client http header. Identifies the
   * scm-manager web interface.
   *
   * @since 1.19
   */
  public static final String SCM_CLIENT_WUI = "WUI";

  /**
   * Url hash separator
   * @since 1.9
   */
  public static final String SEPARATOR_HASH = "#";

  /**
   * Url parameter separator
   * @since 1.9
   */
  public static final String SEPARATOR_PARAMETER = "&";

  /**
   * Url parameters separator
   * @since 1.9
   */
  public static final String SEPARATOR_PARAMETERS = "?";

  /**
   * Url parameter value separator
   * @since 1.9
   */
  public static final String SEPARATOR_PARAMETER_VALUE = "=";

  /**
   * Url folder separator
   * @since 1.5
   */
  public static final String SEPARATOR_PATH = "/";

  /**
   * Url port separator
   * @since 1.5
   */
  public static final String SEPARATOR_PORT = ":";

  /**
   * Url scheme separator
   * @since 1.5
   */
  public static final String SEPARATOR_SCHEME = "://";

  /** message for unauthorized request */
  public static final String STATUS_UNAUTHORIZED_MESSAGE =
    "Authorization Required";

  /** Field description */
  private static final int SKIP_SIZE = 4096;

  /** the logger for HttpUtil */
  private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

  /**
   * Pattern for url normalization
   * @since 1.26
   */
  private static final Pattern PATTERN_URLNORMALIZE =
    Pattern.compile("(?:(http://[^:]+):80(/.+)?|(https://[^:]+):443(/.+)?)");

  /**
   * CharMatcher to select cr/lf and '%' characters
   * @since 1.28
   */
  private static final CharMatcher CRLF_CHARMATCHER =
    CharMatcher.anyOf("\n\r%");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private HttpUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param uri
   * @param suffix
   *
   * @return
   * @since 1.9
   */
  public static String append(String uri, String suffix)
  {
    if (uri.endsWith(SEPARATOR_PATH) && suffix.startsWith(SEPARATOR_PATH))
    {
      uri = uri.substring(0, uri.length() - 1);
    }
    else if (!uri.endsWith(SEPARATOR_PATH) &&!suffix.startsWith(SEPARATOR_PATH))
    {
      uri = uri.concat(SEPARATOR_PATH);
    }

    return uri.concat(suffix);
  }

  /**
   * Method description
   *
   *
   * @param uri
   * @param hash
   *
   * @return
   * @since 1.9
   */
  public static String appendHash(String uri, String hash)
  {
    return uri.concat(SEPARATOR_HASH).concat(hash);
  }

  /**
   * Method description
   *
   *
   * @param uri
   * @param name
   * @param value
   *
   * @return
   * @since 1.9
   */
  public static String appendParameter(String uri, String name, String value)
  {
    String s = null;

    if (uri.contains(SEPARATOR_PARAMETERS))
    {
      s = SEPARATOR_PARAMETERS;
    }
    else
    {
      s = SEPARATOR_PARAMETER;
    }

    return new StringBuilder(uri).append(s).append(name).append(
      SEPARATOR_PARAMETER_VALUE).append(value).toString();
  }

  /**
   * Throws an {@link IllegalArgumentException} if the parameter contains
   * illegal characters which could imply a CRLF injection attack.
   * <stronng>Note:</strong> the current implementation throws the
   * {@link IllegalArgumentException} also if the parameter contains a "%". So
   * you have to decode your parameters before the check,
   *
   * @param parameter value
   *
   * @since 1.28
   */
  public static void checkForCRLFInjection(String parameter)
  {
    if (CRLF_CHARMATCHER.matchesAnyOf(parameter))
    {
      logger.error(
        "parameter \"{}\" contains a character which could be an indicator for a crlf injection",
        parameter);

      throw new IllegalArgumentException(
        "parameter contains an illegal character");
    }
  }

  /**
   * Creates the value for the content-disposition attachment header. The method
   * creates the filename as specified in rfc6266.
   *
   * @param name attachment name
   * @see <a href="http://tools.ietf.org/html/rfc6266#section-5">rfc6266 section 5</a>
   * @return value of content-disposition header
   * @since 1.46
   */
  public static String createContentDispositionAttachmentHeader(String name)
  {
    StringBuilder buffer = new StringBuilder("attachment; ");

    buffer.append("filename=\"").append(name).append("\"; ");
    buffer.append("filename*=utf-8''").append(encode(name));

    return buffer.toString();
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   * @since 1.9
   */
  public static String decode(String value)
  {
    try
    {
      value = URLDecoder.decode(value, ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new RuntimeException("could not decode", ex);
    }

    return value;
  }

  /**
   * Skips to complete body of a request.
   *
   *
   * @param request http request
   *
   * @since 1.37
   */
  public static void drainBody(HttpServletRequest request)
  {
    if (isChunked(request) || (request.getContentLength() > 0))
    {
      InputStream in = null;

      try
      {
        in = request.getInputStream();

        while ((0 < in.skip(SKIP_SIZE)) || (0 <= in.read()))
        {

          // nothing
        }
      }
      catch (IOException e) {}
      finally
      {
        IOUtil.close(in);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   * @since 1.9
   */
  public static String encode(String value)
  {
    try
    {
      value = URLEncoder.encode(value, ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new RuntimeException("could not encode", ex);
    }

    return value;
  }

  /**
   * Returns the normalized url.
   *
   *
   * @param url to normalize
   *
   * @return normalized url
   *
   * @since 1.26
   */
  public static String normalizeUrl(String url)
  {
    if (!Strings.isNullOrEmpty(url))
    {
      Matcher m = PATTERN_URLNORMALIZE.matcher(url);

      if (m.matches())
      {
        String prefix = m.group(1);
        String suffix;

        if (prefix == null)
        {
          prefix = m.group(3);
          suffix = m.group(4);
        }
        else
        {
          suffix = m.group(2);
        }

        if (suffix != null)
        {
          url = prefix.concat(suffix);
        }
        else
        {
          url = prefix;
        }
      }
    }

    return url;
  }

  /**
   * Remove all chars from the given parameter, which could be used for
   * CRLF injection attack. <stronng>Note:</strong> the current implementation
   * the "%" char is also removed from the source parameter.
   *
   * @param parameter value
   *
   * @return the parameter value without crlf chars
   *
   * @since 1.28
   */
  public static String removeCRLFInjectionChars(String parameter)
  {
    return CRLF_CHARMATCHER.removeFrom(parameter);
  }

  /**
   * Method description
   *
   *
   * @param uri
   *
   * @return
   * @since 1.10
   */
  public static String removeMatrixParameter(String uri)
  {
    int index = uri.indexOf(';');

    if (index > 0)
    {
      uri = uri.substring(0, index);
    }

    return uri;
  }

  /**
   * Send an unauthorized header back to the client
   *
   *
   * @param response - the http response
   *
   * @throws IOException
   */
  public static void sendUnauthorized(HttpServletResponse response)
    throws IOException
  {

    sendUnauthorized(null, response, AUTHENTICATION_REALM);
  }

  /**
   * Send an unauthorized header back to the client
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   */
  public static void sendUnauthorized(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    sendUnauthorized(request, response, AUTHENTICATION_REALM);
  }

  /**
   * Send an unauthorized header back to the client
   *
   *
   * @param response - the http response
   * @param realmDescription - realm description
   *
   * @throws IOException
   *
   * @since 1.36
   */
  public static void sendUnauthorized(HttpServletResponse response,
    String realmDescription)
    throws IOException
  {
    sendUnauthorized(null, response, realmDescription);
  }

  /**
   * Send an unauthorized header back to the client
   *
   *
   * @param request http request
   * @param response http response
   * @param realmDescription realm description
   *
   * @throws IOException
   *
   * @since 1.19
   */
  public static void sendUnauthorized(HttpServletRequest request,
    HttpServletResponse response, String realmDescription)
    throws IOException
  {
    if ((request == null) ||!isWUIRequest(request))
    {
      response.setHeader(HEADER_WWW_AUTHENTICATE,
        "Basic realm=\"".concat(realmDescription).concat("\""));

    }
    else if (logger.isTraceEnabled())
    {
      logger.trace(
        "do not send WWW-Authenticate header, because the client is the web interface");
    }

    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
      STATUS_UNAUTHORIZED_MESSAGE);
  }

  /**
   * Returns true if the User-Agent header of the current request starts with
   * the given string.
   *
   *
   * @param request http request
   * @param userAgent string to test against the header
   *
   * @return true if the header starts with the given string
   *
   * @since 1.37
   */
  public static boolean userAgentStartsWith(HttpServletRequest request,
    String userAgent)
  {
    return Strings.nullToEmpty(request.getHeader(HEADER_USERAGENT)).toLowerCase(
      Locale.ENGLISH).startsWith(
      Strings.nullToEmpty(userAgent).toLowerCase(Locale.ENGLISH));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns an absolute url with context path. The method creates the url from
   * forwarding request headers, if they are available.
   *
   *
   * @param request http client request
   * @param pathSegments
   *
   * @return absolute url with context path
   *
   * @see <a href="https://goo.gl/PvGQyH">Issue 748</a>
   * @since 1.16
   */
  public static String getCompleteUrl(HttpServletRequest request,
    String... pathSegments)
  {
    String baseUrl;

    if (isForwarded(request))
    {
      baseUrl = createForwardedBaseUrl(request);
    }
    else
    {
      baseUrl = createBaseUrl(request);
    }

    if (Util.isNotEmpty(pathSegments))
    {
      for (String ps : pathSegments)
      {
        baseUrl = append(baseUrl, ps);
      }
    }

    return baseUrl;
  }

  /**
   * Return the complete url of the given path.
   *
   *
   * @param configuration - main SCM-Manager configuration
   * @param path to get the url from
   *
   * @since 1.5
   *
   * @return the complete url of the given path
   */
  public static String getCompleteUrl(ScmConfiguration configuration,
    String path)
  {
    return append(configuration.getBaseUrl(), path);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param header
   * @param defaultValue
   *
   * @return
   */
  public static String getHeader(HttpServletRequest request, String header,
    String defaultValue)
  {
    String value = request.getHeader(header);

    return Objects.firstNonNull(value, defaultValue);
  }

  /**
   * Returns the port of the url parameter.
   *
   * @param url
   * @return port of url
   */
  public static int getPortFromUrl(String url)
  {
    AssertUtil.assertIsNotEmpty(url);

    int port = PORT_HTTP;
    int schemeIndex = url.indexOf(SEPARATOR_SCHEME);

    if (schemeIndex > 0)
    {
      String urlWithoutScheme = url.substring(schemeIndex
                                  + SEPARATOR_SCHEME.length());
      int portIndex = urlWithoutScheme.lastIndexOf(SEPARATOR_PORT);

      if (portIndex > 0)
      {
        String portString = urlWithoutScheme.substring(portIndex + 1);
        int slIndex = portString.indexOf(SEPARATOR_PATH);

        if (slIndex > 0)
        {
          portString = portString.substring(0, slIndex);
        }

        try
        {
          port = Integer.parseInt(portString);
        }
        catch (NumberFormatException ex)
        {
          logger.error("could not parse port part of url", ex);
        }
      }
      else if (url.startsWith(SCHEME_HTTPS))
      {
        port = PORT_HTTPS;
      }
    }

    return port;
  }

  /**
   * Returns the server port
   *
   *
   * @param configuration
   * @param request
   *
   * @return the server port
   */
  public static int getServerPort(ScmConfiguration configuration,
    HttpServletRequest request)
  {
    int port = PORT_HTTP;
    String baseUrl = configuration.getBaseUrl();

    if (Util.isNotEmpty(baseUrl))
    {
      port = getPortFromUrl(baseUrl);
    }
    else
    {
      port = request.getServerPort();
    }

    return port;
  }

  /**
   * Return the request uri with out the context path.
   *
   *
   * @param request - the http client request
   *
   * @return the request uri with out the context path
   */
  public static String getStrippedURI(HttpServletRequest request)
  {
    return getStrippedURI(request, request.getRequestURI());
  }

  /**
   * Returns the given uri without the context path.
   *
   *
   * @param request - the http client request
   * @param uri - the uri to get the stripped uri from
   *
   * @return uri without context path
   */
  public static String getStrippedURI(HttpServletRequest request, String uri)
  {
    return uri.substring(request.getContextPath().length());
  }

  /**
   * Returns the given uri without ending separator.
   *
   *
   * @param uri - to strip ending separator
   *
   * @return the given uri without a ending separator
   * @since 1.7
   */
  public static String getUriWithoutEndSeperator(String uri)
  {
    if (uri.endsWith(SEPARATOR_PATH))
    {
      uri = uri.substring(0, uri.length() - 1);
    }

    return uri;
  }

  /**
   * Returns the given uri without leading separator.
   *
   *
   * @param uri - to strip leading separator
   *
   * @return the given uri without leading separator
   * @since 1.7
   */
  public static String getUriWithoutStartSeperator(String uri)
  {
    if (uri.startsWith(SEPARATOR_PATH))
    {
      uri = uri.substring(1);
    }

    return uri;
  }

  /**
   * Returns true if the body of the request is chunked.
   *
   *
   * @param request http request
   *
   * @return true if the request is chunked
   *
   * @since 1.37
   */
  public static boolean isChunked(HttpServletRequest request)
  {
    return "chunked".equals(request.getHeader("Transfer-Encoding"));
  }

  /**
   * Returns {@code true} if the request is forwarded by a reverse proxy. The
   * method uses the X-Forwarded-Host header to identify a forwarded request.
   *
   * @param request servlet request
   *
   * @return {@code true} if the request is forwarded
   *
   * @since 1.47
   */
  public static boolean isForwarded(HttpServletRequest request)
  {
    return !Strings.isNullOrEmpty(request.getHeader(HEADER_X_FORWARDED_HOST));
  }

  /**
   * Returns true if the http request is send by the scm-manager web interface.
   *
   *
   * @param request http request
   *
   * @return true if the request comes from the web interface.
   * @since 1.19
   */
  public static boolean isWUIRequest(HttpServletRequest request)
  {
    return SCM_CLIENT_WUI.equalsIgnoreCase(
      request.getHeader(HEADER_SCM_CLIENT));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates base url for request url.
   *
   * @param request http servlet request
   *
   * @return base url from request
   *
   * @since 1.47
   */
  @VisibleForTesting
  static String createBaseUrl(HttpServletRequest request)
  {
    return request.getRequestURL().toString().replace(request.getRequestURI(),
      Util.EMPTY_STRING).concat(request.getContextPath());
  }

  /**
   * Creates base url from forwarding request headers.
   *
   * @param request http servlet request
   *
   * @return base url from forward headers
   *
   * @since 1.47
   */
  @VisibleForTesting
  static String createForwardedBaseUrl(HttpServletRequest request)
  {
    String proto = getHeader(request, HEADER_X_FORWARDED_PROTO,
                     request.getScheme());
    String host;
    String fhost = getHeader(request, HEADER_X_FORWARDED_HOST,
                     request.getScheme());
    String port = request.getHeader(HEADER_X_FORWARDED_PORT);
    int s = fhost.indexOf(SEPARATOR_PORT);

    if (s > 0)
    {
      host = fhost.substring(0, s);

      if (Strings.isNullOrEmpty(port))
      {
        port = fhost.substring(s + 1);
      }
    }
    else
    {
      host = fhost;
    }

    StringBuilder buffer = new StringBuilder(proto);

    buffer.append(SEPARATOR_SCHEME).append(host).append(SEPARATOR_PORT);

    if (Strings.isNullOrEmpty(port))
    {
      buffer.append(String.valueOf(request.getServerPort()));
    }
    else
    {
      buffer.append(port);
    }

    buffer.append(request.getContextPath());

    return buffer.toString();
  }
}
