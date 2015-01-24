/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceiveCommand.Type;

import sonia.scm.repository.GitUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitHookBranchProvider implements HookBranchProvider
{

  /**
   * Constructs ...
   *
   *
   * @param commands
   */
  public GitHookBranchProvider(List<ReceiveCommand> commands)
  {
    Builder<String> createdOrModifiedBuilder = ImmutableList.builder();
    Builder<String> deletedOrClosedBuilder = ImmutableList.builder();

    for (ReceiveCommand command : commands)
    {
      Type type = command.getType();
      String branch = GitUtil.getBranch(command.getRefName());

      if ((type == Type.CREATE) || (type == Type.UPDATE)
        || (type == Type.UPDATE_NONFASTFORWARD))
      {
        createdOrModifiedBuilder.add(branch);
      }
      else if (command.getType() == Type.DELETE)
      {
        deletedOrClosedBuilder.add(branch);
      }
    }

    createdOrModified = createdOrModifiedBuilder.build();
    deletedOrClosed = deletedOrClosedBuilder.build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getCreatedOrModified()
  {
    return createdOrModified;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getDeletedOrClosed()
  {
    return deletedOrClosed;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final List<String> createdOrModified;

  /** Field description */
  private final List<String> deletedOrClosed;
}
