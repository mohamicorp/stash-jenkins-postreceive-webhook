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
        return AJS.contextPath() + '/rest/jenkins/latest/projects/' + pageState.getProject().get("key") +
            '/repos/' + pageState.getRepository().get("slug") + '/' + resourceName;
    }

    function init() {
        var $button = $("#testButton"),
            $jenkinsBase = $("#jenkinsBase"),
            $cloneUrl = $("#gitRepoUrl"),
            $cloneType = $("#cloneType"),
            $ignoreCerts = $("#ignoreCerts"),
            $omitHashCode = $("#omitHashCode"),
            $status,
            defaultUrls;

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

        ajax.rest({
        	url: resourceUrl('config')
        }).success(function(data) {
        	defaultUrls = data;
        });

        $cloneType.change(function() {
        	var val = $(this).val();
        	if (val == "ssh") {
        		$cloneUrl.val( defaultUrls.ssh );
        	} else if (val == "http") {
        		$cloneUrl.val( defaultUrls.http );
        	}
        });

        if ($cloneUrl.val() != "") {
        	var cloneUrl = $cloneUrl.val();
        	if (cloneUrl.search("ssh") === 0) {
        		$cloneType.find("option[value='ssh']").attr("selected", "selected");
        	} else if (cloneUrl.search("http") === 0) {
        		$cloneType.find("option[value='http']").attr("selected", "selected");
        	}
        }

        $button.click(function () {
            setStatus("Trying...", "green");
            setDeleteButtonEnabled(false);
            ajax.rest({
                url: resourceUrl('test'),
                type: 'POST',
                data: {
                    'jenkinsBase': [$jenkinsBase.val()],
                    'gitRepoUrl': [$cloneUrl.val()],
                    'ignoreCerts': [$ignoreCerts.attr('checked') ? "TRUE" : "FALSE"],
                    'omitHashCode': [$omitHashCode.attr('checked') ? "TRUE" : "FALSE"]
                }
            }).always(function () {
                setDeleteButtonEnabled(true)
            }).success(function (data) {
            	if (data.successful) {
                    setStatus("Success!", "green");
            	} else {
            	    setStatus("Error: " + data.message, "red");
            	}
            });
        });
    }

    exports.onReady = function () {
        init();
    }
});
