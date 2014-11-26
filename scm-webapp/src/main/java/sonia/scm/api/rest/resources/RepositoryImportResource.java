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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryAllreadyExistExeption;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.security.Role;
import sonia.scm.util.IOUtil;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.multipart.FormDataParam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Rest resource for importing repositories.
 *
 * @author Sebastian Sdorra
 */
@Path("import/repositories")
@ExternallyManagedLifecycle
public class RepositoryImportResource
{

  /**
   * the logger for RepositoryImportResource
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryImportResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new repository import resource.
   *
   * @param manager repository manager
   * @param serviceFactory
   */
  @Inject
  public RepositoryImportResource(RepositoryManager manager,
    RepositoryServiceFactory serviceFactory)
  {
    this.manager = manager;
    this.serviceFactory = serviceFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Imports a repository type specific bundle. The bundle file is uploaded to
   * the server which is running scm-manager. After the upload has finished, the
   * bundle file is passed to the {@link UnbundleCommandBuilder}. This method
   * requires admin privileges.<br />
   *
   * Status codes:
   * <ul>
   *   <li>201 created</li>
   *   <li>400 bad request, the import bundle feature is not supported by this
   *       type of repositories or the parameters are not valid.</li>
   *   <li>500 internal server error</li>
   *   <li>409 conflict, a repository with the name already exists.</li>
   * </ul>
   *
   * @param uriInfo uri info
   * @param type repository type
   * @param name name of the repository
   * @param inputStream input bundle
   *
   * @return empty response with location header which points to the imported
   *  repository
   */
  @POST
  @Path("{type}/bundle")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response importFromBundle(@Context UriInfo uriInfo,
    @PathParam("type") String type, @FormDataParam("name") String name,
    @FormDataParam("bundle") InputStream inputStream)
  {
    SecurityUtils.getSubject().checkRole(Role.ADMIN);

    checkArgument(!Strings.isNullOrEmpty(name),
      "request does not contain name of the repository");
    checkNotNull(inputStream, "bundle inputStream is required");

    Repository repository;

    try
    {
      Type t = type(type);

      checkSupport(t, Command.UNBUNDLE, "bundle");

      repository = create(type, name);

      RepositoryService service = null;

      File file = File.createTempFile("scm-import-", ".bundle");

      try
      {
        long length = Files.asByteSink(file).writeFrom(inputStream);

        logger.info("copied {} bytes to temp, start bundle import", length);
        service = serviceFactory.create(repository);
        service.getUnbundleCommand().unbundle(file);
      }
      catch (RepositoryException ex)
      {
        handleImportFailure(ex, repository);
      }
      catch (IOException ex)
      {
        handleImportFailure(ex, repository);
      }
      finally
      {
        IOUtil.close(service);
        IOUtil.delete(file);
      }
    }
    catch (IOException ex)
    {
      logger.warn("could not create temporary file", ex);

      throw new WebApplicationException(ex);
    }

    return buildResponse(uriInfo, repository);
  }

  /**
   * This method works exactly like
   * {@link #importFromBundle(UriInfo, String, String, InputStream)}, but this
   * method returns an html content-type. The method exists only for a
   * workaround of the javascript ui extjs. This method requires admin
   * privileges.<br />
   *
   * Status codes:
   * <ul>
   *   <li>201 created</li>
   *   <li>400 bad request, the import bundle feature is not supported by this
   *       type of repositories or the parameters are not valid.</li>
   *   <li>500 internal server error</li>
   *   <li>409 conflict, a repository with the name already exists.</li>
   * </ul>
   *
   *
   * @param uriInfo uri info
   * @param type repository type
   * @param name name of the repository
   * @param inputStream input bundle
   *
   * @return empty response with location header which points to the imported
   *  repository
   */
  @POST
  @Path("{type}/bundle.html")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Response importFromBundleUI(@Context UriInfo uriInfo,
    @PathParam("type") String type, @FormDataParam("name") String name,
    @FormDataParam("bundle") InputStream inputStream)
  {
    return importFromBundle(uriInfo, type, name, inputStream);
  }

  /**
   * Imports a external repository which is accessible via url. The method can
   * only be used, if the repository type supports the {@link Command#PULL}. The
   * method will return a location header with the url to the imported
   * repository. This method requires admin privileges.<br />
   *
   * Status codes:
   * <ul>
   *   <li>201 created</li>
   *   <li>400 bad request, the import by url feature is not supported by this
   *       type of repositories or the parameters are not valid.</li>
   *   <li>409 conflict, a repository with the name already exists.</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param uriInfo uri info
   * @param type repository type
   * @param request request object
   *
   * @return empty response with location header which points to the imported
   *  repository
   */
  @POST
  @Path("{type}/url")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response importFromUrl(@Context UriInfo uriInfo,
    @PathParam("type") String type, UrlImportRequest request)
  {
    SecurityUtils.getSubject().checkRole(Role.ADMIN);
    checkNotNull(request, "request is required");
    checkArgument(!Strings.isNullOrEmpty(request.getName()),
      "request does not contain name of the repository");
    checkArgument(!Strings.isNullOrEmpty(request.getUrl()),
      "request does not contain url of the remote repository");

    Type t = type(type);

    checkSupport(t, Command.PULL, request);

    logger.info("start {} import for external url {}", type, request.getUrl());

    Repository repository = create(type, request.getName());
    RepositoryService service = null;

    try
    {
      service = serviceFactory.create(repository);
      service.getPullCommand().pull(request.getUrl());
    }
    catch (RepositoryException ex)
    {
      handleImportFailure(ex, repository);
    }
    catch (IOException ex)
    {
      handleImportFailure(ex, repository);
    }
    finally
    {
      IOUtil.close(service);
    }

    return buildResponse(uriInfo, repository);
  }

  /**
   * Imports repositories of the given type from the configured repository
   * directory. This method requires admin privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 ok, successful</li>
   *   <li>400 bad request, the import feature is not
   *       supported by this type of repositories.</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param type repository type
   *
   * @return imported repositories
   */
  @POST
  @Path("{type}")
  @TypeHint(Repository[].class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response importRepositories(@PathParam("type") String type)
  {
    SecurityUtils.getSubject().checkRole(Role.ADMIN);

    List<Repository> repositories = new ArrayList<Repository>();

    importFromDirectory(repositories, type);

    //J-
    return Response.ok(
      new GenericEntity<List<Repository>>(repositories) {}
    ).build();
    //J+
  }

  /**
   * Imports repositories of all supported types from the configured repository
   * directories. This method requires admin privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 ok, successful</li>
   *   <li>400 bad request, the import feature is not
   *       supported by this type of repositories.</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @return imported repositories
   */
  @POST
  @TypeHint(Repository[].class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response importRepositories()
  {
    SecurityUtils.getSubject().checkRole(Role.ADMIN);

    logger.info("start directory import for all supported repository types");

    List<Repository> repositories = new ArrayList<Repository>();

    for (Type t : findImportableTypes())
    {
      importFromDirectory(repositories, t.getName());
    }

    //J-
    return Response.ok(
      new GenericEntity<List<Repository>>(repositories) {}
    ).build();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a list of repository types, which support the directory import
   * feature.
   *
   * This method requires admin privileges.<br />
   * <br />
   * Status codes:
   * <ul>
   *   <li>200 ok, successful</li>
   *   <li>400 bad request, the import feature is not
   *       supported by this type of repositories.</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @return list of repository types
   */
  @GET
  @TypeHint(Type[].class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getImportableTypes()
  {
    SecurityUtils.getSubject().checkRole(Role.ADMIN);

    List<Type> types = findImportableTypes();

    //J-
    return Response.ok(
      new GenericEntity<List<Type>>(types) {}
    ).build();
    //J+
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Build rest response for repository.
   *
   *
   * @param uriInfo uri info
   * @param repository imported repository
   *
   * @return rest response
   */
  private Response buildResponse(UriInfo uriInfo, Repository repository)
  {
    URI location = uriInfo.getBaseUriBuilder().path(
                     RepositoryResource.class).path(repository.getId()).build();

    return Response.created(location).build();
  }

  /**
   * Check repository type for support for the given command.
   *
   *
   * @param type repository type
   * @param cmd command
   * @param request request object
   */
  private void checkSupport(Type type, Command cmd, Object request)
  {
    if (!(type instanceof RepositoryType))
    {
      logger.warn("type {} is not a repository type", type.getName());

      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Set<Command> cmds = ((RepositoryType) type).getSupportedCommands();

    if (!cmds.contains(cmd))
    {
      logger.warn("type {} does not support this type of import: {}",
        type.getName(), request);

      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Creates a new repository with the given name and type.
   *
   *
   * @param type repository type
   * @param name repository name
   *
   * @return newly created repository
   */
  private Repository create(String type, String name)
  {
    Repository repository = null;

    try
    {
      repository = new Repository(null, type, name);
      manager.create(repository);
    }
    catch (RepositoryAllreadyExistExeption ex)
    {
      logger.warn("a {} repository with the name {} already exists", ex);

      throw new WebApplicationException(Response.Status.CONFLICT);
    }
    catch (RepositoryException ex)
    {
      handleGenericCreationFailure(ex, type, name);
    }
    catch (IOException ex)
    {
      handleGenericCreationFailure(ex, type, name);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private List<Type> findImportableTypes()
  {
    List<Type> types = new ArrayList<Type>();
    Collection<Type> handlerTypes = manager.getTypes();

    for (Type t : handlerTypes)
    {
      RepositoryHandler handler = manager.getHandler(t.getName());

      if (handler != null)
      {
        try
        {
          if (handler.getImportHandler() != null)
          {
            types.add(t);
          }
        }
        catch (NotSupportedFeatuerException ex)
        {
          if (logger.isTraceEnabled())
          {
            logger.trace("import handler is not supported", ex);
          }
          else if (logger.isInfoEnabled())
          {
            logger.info("{} handler does not support import of repositories",
              t.getName());
          }
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find handler for type {}", t.getName());
      }
    }

    return types;
  }

  /**
   * Handle creation failures.
   *
   *
   * @param ex exception
   * @param type repository type
   * @param name name of the repository
   */
  private void handleGenericCreationFailure(Exception ex, String type,
    String name)
  {
    logger.error(String.format("could not create repository {} with type {}",
      type, name), ex);

    throw new WebApplicationException(ex);
  }

  /**
   * Handle import failures.
   *
   *
   * @param ex exception
   * @param repository repository
   */
  private void handleImportFailure(Exception ex, Repository repository)
  {
    logger.error("import for repository failed, delete repository", ex);

    try
    {
      manager.delete(repository);
    }
    catch (IOException e)
    {
      logger.error("can not delete repository", e);
    }
    catch (RepositoryException e)
    {
      logger.error("can not delete repository", e);
    }

    throw new WebApplicationException(ex,
      Response.Status.INTERNAL_SERVER_ERROR);
  }

  /**
   * Import repositories from a specific type.
   *
   *
   * @param repositories repository list
   * @param type type of repository
   */
  private void importFromDirectory(List<Repository> repositories, String type)
  {
    RepositoryHandler handler = manager.getHandler(type);

    if (handler != null)
    {
      logger.info("start directory import for repository type {}", type);

      try
      {
        List<String> repositoryNames =
          handler.getImportHandler().importRepositories(manager);

        if (repositoryNames != null)
        {
          for (String repositoryName : repositoryNames)
          {
            Repository repository = manager.get(type, repositoryName);

            if (repository != null)
            {
              repositories.add(repository);
            }
            else if (logger.isWarnEnabled())
            {
              logger.warn("could not find imported repository {}",
                repositoryName);
            }
          }
        }
      }
      catch (NotSupportedFeatuerException ex)
      {
        throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
      }
      catch (IOException ex)
      {
        throw new WebApplicationException(ex);
      }
      catch (RepositoryException ex)
      {
        throw new WebApplicationException(ex);
      }
    }
    else
    {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  private Type type(String type)
  {
    RepositoryHandler handler = manager.getHandler(type);

    if (handler == null)
    {
      logger.warn("no handler for type {} found", type);

      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return handler.getType();
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Request for importing external repositories which are accessible via url.
   */
  @XmlRootElement(name = "import")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class UrlImportRequest
  {

    /**
     * Constructs ...
     *
     */
    public UrlImportRequest() {}

    /**
     * Constructs a new {@link UrlImportRequest}
     *
     *
     * @param name name of the repository
     * @param url external url of the repository
     */
    public UrlImportRequest(String name, String url)
    {
      this.name = name;
      this.url = url;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
      //J-
      return Objects.toStringHelper(this)
                    .add("name", name)
                    .add("url", url)
                    .toString();
      //J+
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Returns name of the repository.
     *
     *
     * @return name of the repository
     */
    public String getName()
    {
      return name;
    }

    /**
     * Returns external url of the repository.
     *
     *
     * @return external url of the repository
     */
    public String getUrl()
    {
      return url;
    }

    //~--- fields -------------------------------------------------------------

    /** name of the repository */
    private String name;

    /** external url of the repository */
    private String url;
  }


  //~--- fields ---------------------------------------------------------------

  /** repository manager */
  private final RepositoryManager manager;

  /** repository service factory */
  private final RepositoryServiceFactory serviceFactory;
}
