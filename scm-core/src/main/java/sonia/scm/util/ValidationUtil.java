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

import com.google.common.base.Splitter;
import sonia.scm.Validateable;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ValidationUtil
{

  /** Field description */
  private static final String REGEX_MAIL =
    "^[A-z0-9][\\w.-]*@[A-z0-9][\\w\\-\\.]*\\.[A-z0-9][A-z0-9-]+$";

  /** Field description */
  private static final String REGEX_NAME =
    "^[A-z0-9\\.\\-_@]|[^ ]([A-z0-9\\.\\-_@ ]*[A-z0-9\\.\\-_@]|[^ ])?$";

  /** Field description */
  private static final String REGEX_REPOSITORYNAME =
    "(?!^\\.\\.$)(?!^\\.$)(?!.*[\\\\\\[\\]])^[A-z0-9\\.][A-z0-9\\.\\-_/]*$";

  private static final Pattern REPO_NAME_REGEX = Pattern.compile(REGEX_REPOSITORYNAME);
  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private ValidationUtil() {}

  //~--- get methods ----------------------------------------------------------

  /**
   *  Method description
   *
   *
   *  @param value
   *
   *  @return
   */
  public static boolean isFilenameValid(String value)
  {
    return Util.isNotEmpty(value) && isNotContaining(value, "/", "\\", ":");
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isMailAddressValid(String value)
  {
    return Util.isNotEmpty(value) && value.matches(REGEX_MAIL);
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public static boolean isNameValid(String name)
  {
    return Util.isNotEmpty(name) && name.matches(REGEX_NAME);
  }

  /**
   * Method description
   *
   *
   * @param value
   * @param notAllowedStrings
   *
   * @return
   */
  public static boolean isNotContaining(String value, String... notAllowedStrings) {
    return !Util.isNotEmpty(value) || (notAllowedStrings != null) && Arrays.stream(notAllowedStrings)
                                                                          .noneMatch(value::contains);
  }

  /**
   * Method description
   *
   *
   * @param name
   * @since 1.9
   *
   * @return
   */
  public static boolean isRepositoryNameValid(String name) {
    return Util.isNotEmpty(name) && StreamSupport.stream(Splitter.on('/').split(name).spliterator(), false)
                                                 .map(REPO_NAME_REGEX::matcher)
                                                 .allMatch(Matcher::matches);
  }

  /**
   * Method description
   *
   *
   * @param validateable
   *
   * @return
   */
  public static boolean isValid(Validateable validateable)
  {
    return (validateable != null) && validateable.isValid();
  }
}
