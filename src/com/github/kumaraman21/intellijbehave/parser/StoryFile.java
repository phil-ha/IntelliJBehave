/*
 * Copyright 2011-12 Aman Kumar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kumaraman21.intellijbehave.parser;

import com.github.kumaraman21.intellijbehave.peg.StoryElement;
import com.github.kumaraman21.intellijbehave.peg.StoryPegParserDefinition;
import com.github.kumaraman21.intellijbehave.utility.NodeToPsiElement;
import com.github.kumaraman21.intellijbehave.utility.NodeToStepPsiElement;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.github.kumaraman21.intellijbehave.language.StoryFileType.STORY_FILE_TYPE;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;

public class StoryFile extends PsiFileBase implements StoryElement, Comparable<StoryFile> {
    private static Set<IElementType> immediateChildren = new HashSet<IElementType>();

    static {
        immediateChildren.add(IStoryPegElementType.STORY_DESCRIPTION);
        immediateChildren.add(IStoryPegElementType.STORY_SCENARIO);
        immediateChildren.add(IStoryPegElementType.STORY_GIVEN_STORIES);
        immediateChildren.add(IStoryPegElementType.STORY_META_STATEMENT);
        immediateChildren.add(IStoryPegElementType.STORY_NARRATIVE);
        immediateChildren.add(IStoryPegElementType.STORY_DESCRIPTION);
    }

    public StoryFile(FileViewProvider fileViewProvider) {
        super(fileViewProvider, STORY_FILE_TYPE.getLanguage());
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return STORY_FILE_TYPE;
    }

    public Collection<PsiElement> getStructureViewChildren() {
        Collection<PsiElement> result = new ArrayList<PsiElement>();
        PsiElement story = getStory();
        PsiElement[] children = story.getChildren();
        for (PsiElement child : children) {
            IElementType elementType = child.getNode().getElementType();
            if (immediateChildren.contains(elementType)) {
                result.add(child);
            }
        }
        return result;
    }

    @NotNull
    public List<JBehaveStep> getSteps() {

        List<ASTNode> stepNodes = newArrayList();

        for (PsiElement scenario : getScenarios()) {
            ASTNode[] stepNodesOfScenario = scenario.getNode().getChildren(StoryPegParserDefinition.STEP_TYPES);
            stepNodes.addAll(asList(stepNodesOfScenario));
        }

        return transform(stepNodes, new NodeToStepPsiElement());
    }

    @NotNull
    private List<PsiElement> getScenarios() {
        PsiElement story = getStory();
        if (story == null) {
            return newArrayList();
        }

        ASTNode[] scenarioNodes = story.getNode().getChildren(TokenSet.create(IStoryPegElementType.STORY_SCENARIO));
        return transform(asList(scenarioNodes), new NodeToPsiElement());
    }

    private PsiElement getStory() {
        ASTNode[] storyNodes = this.getNode().getChildren(TokenSet.create(IStoryPegElementType.STORY_STORY));

        if (storyNodes.length > 0) {
            return storyNodes[0].getPsi();
        }

        return null;
    }

    @Override
    public int compareTo(@NotNull StoryFile o) {
        String myCanonicalPath = getVirtualFile().getCanonicalPath();
        String otherCanonicalPath = o.getVirtualFile().getCanonicalPath();
        if (myCanonicalPath == null) return -1;
        if (otherCanonicalPath == null) return 1;
        return myCanonicalPath.compareTo(otherCanonicalPath);
    }
}
