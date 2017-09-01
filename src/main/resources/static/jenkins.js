define('plugin/jenkins/test', [
    'jquery',
    'aui',
    'bitbucket/internal/util/ajax',
    'bitbucket/util/navbuilder',
    'bitbucket/internal/model/page-state',
    'bitbucket/internal/util/error',
    'exports'
], function ($, AJS, ajax, navBuilder, pageState, errorUtil, exports) {
    function resourceUrl(resourceName) {
        return AJS.contextPath() + '/rest/jenkins/latest/projects/' + pageState.getProject().getKey() +
            '/repos/' + pageState.getRepository().getSlug() + '/' + resourceName;
    }

    function init() {
        var $button = $("#testButton"),
            $jenkinsBase = $("#jenkinsBase"),
            $cloneUrl = $("#gitRepoUrl"),
            $cloneType = $("#cloneType"),
            $ignoreCerts = $("#ignoreCerts"),
            $omitHashCode = $("#omitHashCode"),
            $omitBranchName = $("#omitBranchName"),
            $omitTargetBranch = $("#omitTargetBranch"),
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
                $button.removeAttr("disabled").removeClass("disabled");
            } else {
                $button.attr("disabled", "disabled").addClass("disabled");
            }
        }

        function setCloneUrl(val) {
            if (val == "ssh") {
        		$cloneUrl.val( defaultUrls.ssh );
                $cloneUrl.attr("disabled", "disabled").addClass("disabled");
        	} else if (val == "http") {
        		$cloneUrl.val( defaultUrls.http );
                $cloneUrl.attr("disabled", "disabled").addClass("disabled");
        	} else {
                $cloneUrl.removeAttr("disabled").removeClass("disabled");
            }
        }

        $cloneType.change(function() {
        	setCloneUrl($(this).val());
        });

        ajax.rest({
        	url: resourceUrl('config')
        }).success(function(data) {
        	defaultUrls = data;
            
            if ($cloneUrl.val() != "") {
                var cloneUrl = $cloneUrl.val();
                if (cloneUrl == defaultUrls.ssh) {
                    $cloneType.find("option[value='ssh']").attr("selected", "selected");
                } else if (cloneUrl == defaultUrls.http) {
                    $cloneType.find("option[value='http']").attr("selected", "selected");
                } else {
                    $cloneType.find("option[value='custom']").attr("selected", "selected");
                }
                $cloneType.trigger('change');
            } else {
                setCloneUrl($cloneType.val());
            }
        });

        $button.click(function () {
            setStatus("Trying...", "green");
            setDeleteButtonEnabled(false);
            ajax.rest({
                url: resourceUrl('test'),
                type: 'POST',
                data: {
                    'jenkinsBase': [$jenkinsBase.val()],
                    'cloneType': [$cloneType.val()],
                    'gitRepoUrl': [$cloneUrl.val()],
                    'ignoreCerts': [$ignoreCerts.attr('checked') ? "TRUE" : "FALSE"],
                    'omitHashCode': [$omitHashCode.attr('checked') ? "TRUE" : "FALSE"],
                    'omitBranchName': [$omitBranchName.attr('checked') ? "TRUE" : "FALSE"],
                    'omitTargetBranch': [$omitTargetBranch.attr('checked') ? "TRUE" : "FALSE"]
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
