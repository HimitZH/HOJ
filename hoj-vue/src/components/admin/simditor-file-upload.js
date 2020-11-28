/* eslint-disable */

import Simditor from 'tar-simditor'
import * as $ from 'jquery'

var UploadFile,
  __hasProp = {}.hasOwnProperty,
  __extends = function (child, parent) {
    for (var key in parent) {
      if (__hasProp.call(parent, key)) child[key] = parent[key];
    }

    function ctor() {
      this.constructor = child;
    }

    ctor.prototype = parent.prototype;
    child.prototype = new ctor();
    child.__super__ = parent.prototype;
    return child;
  },
  __slice = [].slice;

UploadFile = (function (_super) {
  __extends(UploadFile, _super);

  UploadFile.i18n = {
    'zh-CN': {
      uploadfile: '上传文件'
    },
    'en-US': {
      uploadfile: 'upload file'
    }
  };

  UploadFile.prototype.name = 'uploadfile';

  UploadFile.prototype.icon = 'upload';

  function UploadFile() {
    var args;
    args = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    UploadFile.__super__.constructor.apply(this, args);
    this._initUpload();
  }

  UploadFile.prototype._initUpload = function () {
    this.input = $('<input />', {
      type: 'file',
      style: 'position:absolute;top:0;right:0;height:100%;width:100%;opacity:0;filter:alpha(opacity=0);cursor:pointer;'
    }).prependTo(this.el)
    var _this = this;
    this.el.on('click mousedown', 'input[type=file]', function (e) {
      return e.stopPropagation();
    }).on('change', 'input[type=file]', function (e) {

      var formData = new FormData();
      formData.append('file', this.files[0]);
      $.ajax({
        url: '/api/admin/upload_file',
        type: 'POST',
        cache: false,
        data: formData,
        processData: false,
        contentType: false
      }).done(function (res) {
        if (!res.success) {
          alert("upload file failed")
        } else {
          let link = '<a target="_blank" className="simditor-attach-link" href="' + res.file_path + '">' + res.file_name + '</a>'
          _this.editor.setValue(_this.editor.getValue() + link)
        }
      }).fail(function (res) {
        alert("upload file failed")
      });
    });
  }

  return UploadFile;

})(Simditor.Button);

Simditor.Toolbar.addButton(UploadFile);


