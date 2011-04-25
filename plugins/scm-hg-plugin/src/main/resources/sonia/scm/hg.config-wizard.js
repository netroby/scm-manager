/*
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

Ext.ns("Sonia.hg");

Sonia.hg.ConfigWizard = Ext.extend(Ext.Window,{
  
  hgConfig: null,
  title: 'Mercurial Configuration Wizard',
  
  initComponent: function(){
    
    this.addEvents('finish');
    
    var config = {
      title: this.title,
      layout: 'fit',
      width: 540,
      height: 320,
      closable: true,
      resizable: true,
      plain: true,
      border: false,
      modal: true,
      bodyCssClass: 'x-panel-mc',
      items: [{
        id: 'hgConfigWizardPanel',
        xtype: 'hgConfigWizardPanel',
        hgConfig: this.hgConfig,
        listeners: {
          finish: {
            fn: this.onFinish,
            scope: this
          }
        }
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.hg.ConfigWizard.superclass.initComponent.apply(this, arguments);
  },
  
  onFinish: function(config){
    this.fireEvent('finish', config);
    this.close();
  }
  
});

Sonia.hg.InstallationJsonReader = function(){
  this.RecordType = Ext.data.Record.create([{
    name: "path",
    mapping: "path",
    type: "string"
  }]);
};

Ext.extend(Sonia.hg.InstallationJsonReader, Ext.data.JsonReader, {
  
  readRecords: function(o){
    this.jsonData = o;
    
    if (debug){
      console.debug('read installation data from json');
      console.debug(o);
    }
    
    var records = [];
    var paths = o.path;
    for ( var i=0; i<paths.length; i++ ){
      records.push(new this.RecordType({
        'path': paths[i]
      }));
    }
    return {
      success: true,
      records: records,
      totalRecords: records.length
    };
  }
  
});

Sonia.hg.ConfigWizardPanel = Ext.extend(Ext.Panel,{
  
  hgConfig: null,
  
  initComponent: function(){
    
    var navHandler = function(direction) {
      var layout = this.getLayout();
      var i = layout.activeItem.id.split('step-')[1];
      i = parseInt(i) - 1;
      var next = parseInt(i) + direction;
      layout.setActiveItem(next);
      Ext.getCmp('move-prev').setDisabled(next == 0);
      Ext.getCmp('move-next').setDisabled(next == 2);
      Ext.getCmp('finish').setDisabled(next != 2);
    }; 
    
    this.addEvents('finish');
    
    var hgInstallationStore = new Ext.data.Store({
      proxy: new  Ext.data.HttpProxy({
        url: restUrl + 'config/repositories/hg/installations/hg.json'
      }),
      fields: [ 'path' ],
      reader: new Sonia.hg.InstallationJsonReader(),
      autoLoad: true,
      autoDestroy: true
    });
    
    var pythonInstallationStore = new Ext.data.Store({
      proxy: new  Ext.data.HttpProxy({
        url: restUrl + 'config/repositories/hg/installations/python.json'
      }),
      fields: [ 'path' ],
      reader: new Sonia.hg.InstallationJsonReader(),
      autoLoad: true,
      autoDestroy: true
    });
    
    var config = {
      layout: 'card',
      activeItem: 0,
      bodyStyle: 'padding: 5px',
      defaults: {
        bodyCssClass: 'x-panel-mc',
        border: false
      },
      bbar: ['->',{
        id: 'move-prev',
        text: 'Back',
        handler: navHandler.createDelegate(this, [-1]),
        disabled: true,
        scope: this
      },{
        id: 'move-next',
        text: 'Next',
        handler: navHandler.createDelegate(this, [1]),
        scope: this
      },{
        id: 'finish',
        text: 'Finish',
        handler: function(){
          if ( debug ){
            console.debug('finish');
            console.debug( this.hgConfig );
          }
          this.fireEvent('finish', this.hgConfig);
        },
        scope: this,
        disabled: true
      }],
      items: [{
        id: 'step-1',
        layout: 'form',
        items: [{
          fieldLabel: 'Mercurial Installation',
          name: 'mercurial',
          xtype: 'combo',
          readOnly: false,
          triggerAction: 'all',
          lazyRender: true,
          mode: 'local',
          editable: true,
          store: hgInstallationStore,
          valueField: 'path',
          displayField: 'path',
          allowBlank: false
        },{
          fieldLabel: 'Python Installation',
          name: 'python',
          xtype: 'combo',
          readOnly: false,
          triggerAction: 'all',
          lazyRender: true,
          mode: 'local',
          editable: true,
          store: pythonInstallationStore,
          valueField: 'path',
          displayField: 'path',
          allowBlank: false          
        }]
      },{
        id: 'step-2',
        html: '<h2>Step 2</h2>'
      },{
        id: 'step-3',
        html: '<h2>Step 3</h2>'
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.hg.ConfigWizardPanel.superclass.initComponent.apply(this, arguments);
  }
  
});

// register xtype
Ext.reg('hgConfigWizardPanel', Sonia.hg.ConfigWizardPanel);