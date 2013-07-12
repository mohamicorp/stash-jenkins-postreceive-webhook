define('plugin/jenkins/test', [
    'jquery',
    'aui',
    'util/ajax',
    'util/navbuilder',
    'model/page-state',
    'util/error',
    'exports'
], function ($, AJS, ajax, navBuilder, pageState, errorUtil, exports) {
    function resourceUrl(resourceName) {
        return AJS.contextPath() + '/rest/jenkins/latest/projects/' + pageState.getProject().key +
            '/repos/' + pageState.getRepository().slug + '/' + resourceName;
    }

    function init() {
        var $button = $("#testButton"),
            $jenkinsBase = $("#jenkinsBase"),
            $cloneType = $("#cloneType"),
            $httpUsername = $("#httpUsername"),
            $ignoreCerts = $("#ignoreCerts"),
            $status;

        function setStatus(status, color) {
            if ($status == null) {
                $status = $("<span style='margin-left: 10px;'></span>").insertAfter($button);
            }
            $status.text(status);
            $status.css("color", color);
        }

        function setDeleteButtonEnabled(enabled) {
            if (enabled) {
                $button.removeProp("disabled").removeClass("disabled");
            } else {
                $button.prop("disabled", "disabled").addClass("disabled");
            }
        }
        
        $cloneType.change(function() {
        	if ($(this).val() == "http") {
        		$httpUsername.parent().show();
        	} else {
        		$httpUsername.parent().hide();
        	}
        }).change();

        $button.click(function () {
            setStatus("Trying...", "green");
            setDeleteButtonEnabled(false);
            ajax.rest({
                url: resourceUrl('test'),
                type: 'POST',
                data: {
                    'jenkinsBase': [$jenkinsBase.val()],
                    'cloneType': [$cloneType.val()],
                    'httpUserName': [$httpUsername.val()],
                    'ignoreCerts': [$ignoreCerts.attr('checked') ? "TRUE" : "FALSE"]
                }
            }).always(function () {
                    setDeleteButtonEnabled(true)
                }).success(function () {
                    setStatus("Success!", "green");
                }).error(function () {
                    setStatus("It didn't work!", "red");
                });
        });
    }

    exports.onReady = function () {
        init();
    }
});