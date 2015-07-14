/////////////////////////////////////////////////////////////
// SubjectServiceImpl.java
// gooru-api
// Created by Gooru on 2015
// Copyright (c) 2015 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person      obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so,  subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY  KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE    WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR  PURPOSE     AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR  COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
package org.ednovo.gooru.domain.service.subject;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ednovo.gooru.application.util.GooruImageUtil;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.Subject;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.exception.BadRequestException;
import org.ednovo.gooru.domain.service.BaseServiceImpl;
import org.ednovo.gooru.infrastructure.persistence.hibernate.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

@Service
public class SubjectServiceImpl extends BaseServiceImpl implements SubjectService, ParameterProperties, ConstantProperties {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private GooruImageUtil gooruImageUtil;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ActionResponseDTO<Subject> createSubject(Subject subject, User user) {
		final Errors errors = validateSubject(subject);
		if (!errors.hasErrors()) {
			subject.setClassificationTypeId(subject.getClassificationTypeId());
			subject.setCreatedOn(new Date(System.currentTimeMillis()));
			subject.setLastModified(new Date(System.currentTimeMillis()));
			subject.setActiveFlag((short) 1);
			subject.setCreator(user);
			subject.setDisplaySequence(this.getSubjectRepository().getMaxSequence() + 1);
			this.getSubjectRepository().save(subject);
		}
		return new ActionResponseDTO<Subject>(subject, errors);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Subject getSubject(Integer subjectId) {
		Subject subject = this.getSubjectRepository().getSubject(subjectId);
		rejectIfNull(subject, GL0056, 404, SUBJECT);
		reject((subject.getActiveFlag() == 1), GL0107, 400, SUBJECT);
		if (subject.getImagePath() != null) {
			subject.setThumbnails(GooruImageUtil.getThumbnails(subject.getImagePath()));
		}
		return subject;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Map<String, Object>> getCourses(int offset, int limit, int subjectId) {
		List<Map<String, Object>> courses = this.getSubjectRepository().getCourses(offset, limit, subjectId);
		if (courses != null) {
			for (Map<String, Object> course : courses) {
				if (course.get(IMAGE_PATH) != null) {
					course.put(THUMBNAILS, GooruImageUtil.getThumbnails(course.get(IMAGE_PATH)));
				}
			}
		}
		return courses;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteSubject(Integer subjectId) {
		Subject subject = this.getSubjectRepository().getSubject(subjectId);
		rejectIfNull(subject, GL0056, 404, SUBJECT);
		subject.setActiveFlag((short) 0);
		subject.setLastModified(new Date(System.currentTimeMillis()));
		this.getSubjectRepository().save(subject);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Subject> getSubjects(Integer classificationTypeId, Integer limit, Integer offset) {
		List<Subject> subjects = this.getSubjectRepository().getSubjects(classificationTypeId, limit, offset);
		if (subjects != null) {
			for (Subject subject : subjects) {
				if (subject.getImagePath() != null) {
					subject.setThumbnails(GooruImageUtil.getThumbnails(subject.getImagePath()));
				}
			}
		}
		return subjects;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void updateSubject(Subject newSubject, Integer subjectId) {
		Subject subject = this.getSubjectRepository().getSubject(subjectId);
		rejectIfNull(subject, GL0056, 404, SUBJECT);
		if (newSubject.getClassificationTypeId() != null) {
			this.rejectIfInvalidType(newSubject.getClassificationTypeId(), CLASSIFICATION_TYPE_ID, GL0007, Constants.CLASSIFICATION_TYPE);
			subject.setClassificationTypeId(newSubject.getClassificationTypeId());
		}
		if (newSubject.getActiveFlag() != null) {
			reject((newSubject.getActiveFlag() == 0 || newSubject.getActiveFlag() == 1), GL0007, 400, ACTIVE_FLAG);
			subject.setActiveFlag(newSubject.getActiveFlag());
		}
		if (newSubject.getDescription() != null) {
			subject.setDescription(newSubject.getDescription());
		}

		if (newSubject.getName() != null) {
			subject.setName(newSubject.getName());
		}
		if (newSubject.getMediaFilename() != null) {
			StringBuilder basePath = new StringBuilder(Subject.REPO_PATH);
			basePath.append(File.separator).append(subjectId);
			this.getGooruImageUtil().imageUpload(newSubject.getMediaFilename(), basePath.toString(), Subject.IMAGE_DIMENSION);
			basePath.append(File.separator).append(newSubject.getMediaFilename());
			subject.setImagePath(basePath.toString());
		}
		subject.setLastModified(new Date(System.currentTimeMillis()));
		this.getSubjectRepository().save(subject);
	}

	private Errors validateSubject(Subject subject) {
		final Errors errors = new BindException(subject, SUBJECT);
		rejectIfNull(errors, subject.getName(), NAME, generateErrorMessage(GL0006, NAME));
		rejectIfNull(errors, subject.getClassificationTypeId(), CLASSIFICATION_TYPE_ID, generateErrorMessage(GL0006, CLASSIFICATION_TYPE_ID));
		if (subject.getClassificationTypeId() != null) {
			rejectIfInvalidType(errors, subject.getClassificationTypeId(), CLASSIFICATION_TYPE_ID, GL0007, generateErrorMessage(GL0007, CLASSIFICATION_TYPE_ID), Constants.CLASSIFICATION_TYPE);
		}
		return errors;
	}

	private void rejectIfInvalidType(Object data, String message, String code, Map<Object, String> typeParam) {
		if (!typeParam.containsKey(data)) {
			throw new BadRequestException(generateErrorMessage(code, message), code);
		}
	}

	public SubjectRepository getSubjectRepository() {
		return subjectRepository;
	}

	public GooruImageUtil getGooruImageUtil() {
		return gooruImageUtil;
	}
}
