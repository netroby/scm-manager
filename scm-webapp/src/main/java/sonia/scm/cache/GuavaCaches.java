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


package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sebastian Sdorra
 */
public final class GuavaCaches
{

  /**
   * the logger for GuavaCaches
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaCaches.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private GuavaCaches() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   * @param name
   *
   * @return
   */
  public static com.google.common.cache.Cache create(
    GuavaCacheConfiguration configuration, String name)
  {
    CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

    if (configuration.getConcurrencyLevel() != null)
    {
      builder.concurrencyLevel(configuration.getConcurrencyLevel());
    }

    if (configuration.getExpireAfterAccess() != null)
    {
      builder.expireAfterAccess(configuration.getExpireAfterAccess(),
        TimeUnit.SECONDS);
    }

    if (configuration.getExpireAfterWrite() != null)
    {
      builder.expireAfterWrite(configuration.getExpireAfterWrite(),
        TimeUnit.SECONDS);
    }

    if (configuration.getInitialCapacity() != null)
    {
      builder.initialCapacity(configuration.getInitialCapacity());
    }

    if (configuration.getMaximumSize() != null)
    {
      builder.maximumSize(configuration.getMaximumSize());
    }

    if (configuration.getMaximumWeight() != null)
    {
      builder.maximumWeight(configuration.getMaximumWeight());
    }

    if (isEnabled(configuration.getRecordStats()))
    {
      builder.recordStats();
    }

    if (isEnabled(configuration.getSoftValues()))
    {
      builder.softValues();
    }

    if (isEnabled(configuration.getWeakKeys()))
    {
      builder.weakKeys();
    }

    if (isEnabled(configuration.getWeakValues()))
    {
      builder.weakKeys();
    }

    if (logger.isTraceEnabled())
    {
      logger.trace("create new cache {} from builder: {}", name, builder);
    }

    return builder.build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  private static boolean isEnabled(Boolean v)
  {
    return (v != null) && v;
  }
}
