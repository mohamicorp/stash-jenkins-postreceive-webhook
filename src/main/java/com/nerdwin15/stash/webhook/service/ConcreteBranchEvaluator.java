package com.nerdwin15.stash.webhook.service;

import java.util.Collection;

import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * A concrete implementation of the {@link BranchEvaluator} that uses sample
 * code from the Atlassian stash-webhook-plugin:
 *
 * https://bitbucket.org/atlassian/stash-webhook-plugin/src/a18713fad2959e670e355df64c840b79a806d8ab/src/main/java/com/atlassian/stash/plugin/webook/WebHook.java?at=master
 *
 * @author Michael Irwin (mikesir87)
 */
public class ConcreteBranchEvaluator implements BranchEvaluator {

  private static final String REFS_HEADS = "refs/heads/";

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterable<String> getBranches(Collection<RefChange> refChanges) {
    return Iterables.transform(
        Iterables.filter(refChanges, new Predicate<RefChange>() {
          @Override
          public boolean apply(RefChange input) {
            // We only care about non-deleted branches
            return input.getType() != RefChangeType.DELETE
                && input.getRef().getId().startsWith(REFS_HEADS);
          }
        }), new Function<RefChange, String>() {
          @Override
          public String apply(RefChange input) {
            // Not 100% sure whether this is _just_ branch or is full ref?
            return input.getRef().getId().replace(REFS_HEADS, "");
          }
        });
  }

}
