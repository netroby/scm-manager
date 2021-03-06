/* *
 * Copyright (c) 2014, Sebastian Sdorra
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


Sonia.repository.TagComboBox = Ext.extend(Ext.form.ComboBox, {
  
  repositoryId: null,

  initComponent: function(){    
    var tagStore = new Sonia.rest.JsonStore({
      proxy: new Ext.data.HttpProxy({
        url: restUrl + 'repositories/' + this.repositoryId + '/tags.json',
        method: 'GET',
        disableCaching: false
      }),
      root: 'tag',
      idProperty: 'name',
      fields: [ 'name', 'revision' ]
    });

    var config = {
      valueField: 'revision',
      displayField: 'name',
      typeAhead: false,
      editable: false,
      triggerAction: 'all',
      store: tagStore
    };
      
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.TagComboBox.superclass.initComponent.apply(this, arguments);
  }
  
});

// register xtype
Ext.reg('repositoryTagComboBox', Sonia.repository.TagComboBox);