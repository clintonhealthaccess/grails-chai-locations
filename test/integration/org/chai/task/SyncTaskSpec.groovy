package org.chai.task

/*
 * Copyright (c) 2012, Clinton Health Access Initiative.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Clinton Health Access Initiative nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CLINTON HEALTH ACCESS INITIATIVE BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.chai.location.IntegrationTests
import org.chai.location.SyncService

import org.apache.commons.io.FileUtils

class SyncTaskSpec extends IntegrationTests {
		
	def "test output when exception occurs"() {
		setup:
		def task = new SyncTask()
		task.syncService = new SyncService()
		task.syncService.metaClass.syncFromFullList = {throw new Exception("exception occurred")}
		def tmpDir = new File('tmp')
		tmpDir.mkdir()
		task.metaClass.getFolder = {return tmpDir}
		
		when:
		task.executeTask()
		
		then:
		new File(task.getFolder(), task.getOutputFilename()).text == "An exception occurred trying to sync locations: exception occurred"
		
		cleanup:
		FileUtils.deleteDirectory(tmpDir)
	}
	
	def "test output when everything is ok"() {
		setup:
		def task = new SyncTask()
		task.syncService = new SyncService()
		task.syncService.metaClass.syncFromFullList = {return}
		def tmpDir = new File('tmp')
		tmpDir.mkdir()
		task.metaClass.getFolder = {return tmpDir}

		when:
		task.executeTask()

		then:
		new File(task.getFolder(), task.getOutputFilename()).text == "Location sync was successful."

		cleanup:
		FileUtils.deleteDirectory(tmpDir)
	}
	
}