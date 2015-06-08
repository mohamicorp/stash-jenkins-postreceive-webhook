define('plugin/jenkins/pr-triggerbutton', [
  'jquery',
  'aui',
  'model/page-state'
], function($, AJS, pageState) {
  
  var getResourceUrl = function() {
    return AJS.contextPath() + '/rest/jenkins/latest/projects/' 
      + pageState.getProject().getKey() + '/repos/' 
      + pageState.getRepository().getSlug() + '/triggerJenkins'
      + '?branches=' + pageState.getPullRequest().getFromRef().getDisplayId()
      + '&sha1=' + pageState.getPullRequest().getFromRef().getLatestChangeset();
  };

  var waiting = '<span class="aui-icon aui-icon-wait">Wait</span>';
  
  $(".triggerJenkinsBuild").click(function() {
    var $this = $(this);
    var text = $this.text();

    $this.attr("disabled", "disabled").html(waiting + " " + text);
  
    $.post(getResourceUrl(), function() {
      // Place in timer for UI-happiness - might go "too quick" and not notice
      // it actually triggered
      setTimeout(function() {  
          $this.removeAttr("disabled").text(text);
      }, 500);
    });
    return false;
  });

});

AJS.$(document).ready(function() {
    require('plugin/jenkins/pr-triggerbutton');
});
