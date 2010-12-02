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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.plugin.ext.ExtensionProcessor;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.XmlAuthenticationManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BindingExtensionProcessor implements ExtensionProcessor
{

  /** the logger for BindingExtensionProcessor */
  private static final Logger logger =
    LoggerFactory.getLogger(BindingExtensionProcessor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   */
  public BindingExtensionProcessor()
  {
    this.moduleSet = new HashSet<Module>();
    this.extensions = new HashSet<Class<?>>();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param binder
   */
  @SuppressWarnings("unchecked")
  public void bindExtensions(Binder binder)
  {
    Multibinder<RepositoryHandler> repositoryHandlers =
      Multibinder.newSetBinder(binder, RepositoryHandler.class);
    Multibinder<AuthenticationManager> authenticators =
      Multibinder.newSetBinder(binder, AuthenticationManager.class);

    authenticators.addBinding().to(XmlAuthenticationManager.class);

    for (Class extensionClass : extensions)
    {
      try
      {
        if (RepositoryHandler.class.isAssignableFrom(extensionClass))
        {
          if (logger.isInfoEnabled())
          {
            logger.info("bind RepositoryHandler {}", extensionClass.getName());
          }

          binder.bind(extensionClass);
          repositoryHandlers.addBinding().to(extensionClass);
        }
        else if (EncryptionHandler.class.isAssignableFrom(extensionClass))
        {
          bind(binder, EncryptionHandler.class, extensionClass);
        }
        else if (AuthenticationManager.class.isAssignableFrom(extensionClass))
        {
          if (logger.isInfoEnabled())
          {
            logger.info("bind Authenticator {}", extensionClass.getName());
          }

          binder.bind(extensionClass);
          authenticators.addBinding().to(extensionClass);
        }
        else
        {
          if (logger.isInfoEnabled())
          {
            logger.info("bind {}", extensionClass.getName());
          }

          binder.bind(extensionClass);
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param extension
   * @param extensionClass
   */
  @Override
  public void processExtension(Extension extension, Class extensionClass)
  {
    if (Module.class.isAssignableFrom(extensionClass))
    {
      if (logger.isInfoEnabled())
      {
        logger.info("add GuiceModule {}", extensionClass.getName());
      }

      addModuleClass(extensionClass);
    }
    else
    {
      extensions.add(extensionClass);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<Module> getModuleSet()
  {
    return moduleSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param extensionClass
   */
  private void addModuleClass(Class<? extends Module> extensionClass)
  {
    try
    {
      Module module = extensionClass.newInstance();

      moduleSet.add(module);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage(), ex);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param binder
   * @param type
   * @param bindingType
   * @param <T>
   *
   * @return
   */
  private <T> void bind(Binder binder, Class<T> type,
                        Class<? extends T> bindingType)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("bind Authenticator {}", type.getName(),
                   bindingType.getName());
    }

    binder.bind(type).to(bindingType);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<Class<?>> extensions;

  /** Field description */
  private Set<Module> moduleSet;
}
