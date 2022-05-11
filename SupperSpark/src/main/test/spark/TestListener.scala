package spark

import org.apache.spark.scheduler.{JobSucceeded, SparkListener, SparkListenerApplicationEnd, SparkListenerApplicationStart, SparkListenerBlockManagerAdded, SparkListenerBlockManagerRemoved, SparkListenerBlockUpdated, SparkListenerEnvironmentUpdate, SparkListenerEvent, SparkListenerExecutorAdded, SparkListenerExecutorBlacklisted, SparkListenerExecutorBlacklistedForStage, SparkListenerExecutorExcluded, SparkListenerExecutorExcludedForStage, SparkListenerExecutorMetricsUpdate, SparkListenerExecutorRemoved, SparkListenerExecutorUnblacklisted, SparkListenerExecutorUnexcluded, SparkListenerJobEnd, SparkListenerJobStart, SparkListenerNodeBlacklisted, SparkListenerNodeBlacklistedForStage, SparkListenerNodeExcluded, SparkListenerNodeExcludedForStage, SparkListenerNodeUnblacklisted, SparkListenerNodeUnexcluded, SparkListenerResourceProfileAdded, SparkListenerSpeculativeTaskSubmitted, SparkListenerStageCompleted, SparkListenerStageExecutorMetrics, SparkListenerStageSubmitted, SparkListenerTaskEnd, SparkListenerTaskGettingResult, SparkListenerTaskStart, SparkListenerUnpersistRDD, SparkListenerUnschedulableTaskSetAdded, SparkListenerUnschedulableTaskSetRemoved}

class TestListener extends SparkListener{
  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = super.onStageCompleted(stageCompleted)

  override def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit = super.onStageSubmitted(stageSubmitted)

  override def onTaskStart(taskStart: SparkListenerTaskStart): Unit = {
    val taskId = taskStart.stageAttemptId
    val stageId = taskStart.stageId;
    taskStart.taskInfo
  }

  override def onTaskGettingResult(taskGettingResult: SparkListenerTaskGettingResult): Unit = super.onTaskGettingResult(taskGettingResult)

  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    taskEnd.taskType
  }

  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    println(jobStart.time+":"+jobStart.jobId)
  }

  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
      println(jobEnd.jobId+":"+jobEnd.time+":"+jobEnd.jobResult)
  }

  override def onEnvironmentUpdate(environmentUpdate: SparkListenerEnvironmentUpdate): Unit = super.onEnvironmentUpdate(environmentUpdate)

  override def onBlockManagerAdded(blockManagerAdded: SparkListenerBlockManagerAdded): Unit = super.onBlockManagerAdded(blockManagerAdded)

  override def onBlockManagerRemoved(blockManagerRemoved: SparkListenerBlockManagerRemoved): Unit = super.onBlockManagerRemoved(blockManagerRemoved)

  override def onUnpersistRDD(unpersistRDD: SparkListenerUnpersistRDD): Unit = super.onUnpersistRDD(unpersistRDD)

  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = super.onApplicationStart(applicationStart)

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = super.onApplicationEnd(applicationEnd)

  override def onExecutorMetricsUpdate(executorMetricsUpdate: SparkListenerExecutorMetricsUpdate): Unit = super.onExecutorMetricsUpdate(executorMetricsUpdate)

  override def onStageExecutorMetrics(executorMetrics: SparkListenerStageExecutorMetrics): Unit = super.onStageExecutorMetrics(executorMetrics)

  override def onExecutorAdded(executorAdded: SparkListenerExecutorAdded): Unit = super.onExecutorAdded(executorAdded)

  override def onExecutorRemoved(executorRemoved: SparkListenerExecutorRemoved): Unit = super.onExecutorRemoved(executorRemoved)

  override def onExecutorBlacklisted(executorBlacklisted: SparkListenerExecutorBlacklisted): Unit = super.onExecutorBlacklisted(executorBlacklisted)

  override def onExecutorExcluded(executorExcluded: SparkListenerExecutorExcluded): Unit = super.onExecutorExcluded(executorExcluded)

  override def onExecutorBlacklistedForStage(executorBlacklistedForStage: SparkListenerExecutorBlacklistedForStage): Unit = super.onExecutorBlacklistedForStage(executorBlacklistedForStage)

  override def onExecutorExcludedForStage(executorExcludedForStage: SparkListenerExecutorExcludedForStage): Unit = super.onExecutorExcludedForStage(executorExcludedForStage)

  override def onNodeBlacklistedForStage(nodeBlacklistedForStage: SparkListenerNodeBlacklistedForStage): Unit = super.onNodeBlacklistedForStage(nodeBlacklistedForStage)

  override def onNodeExcludedForStage(nodeExcludedForStage: SparkListenerNodeExcludedForStage): Unit = super.onNodeExcludedForStage(nodeExcludedForStage)

  override def onExecutorUnblacklisted(executorUnblacklisted: SparkListenerExecutorUnblacklisted): Unit = super.onExecutorUnblacklisted(executorUnblacklisted)

  override def onExecutorUnexcluded(executorUnexcluded: SparkListenerExecutorUnexcluded): Unit = super.onExecutorUnexcluded(executorUnexcluded)

  override def onNodeBlacklisted(nodeBlacklisted: SparkListenerNodeBlacklisted): Unit = super.onNodeBlacklisted(nodeBlacklisted)

  override def onNodeExcluded(nodeExcluded: SparkListenerNodeExcluded): Unit = super.onNodeExcluded(nodeExcluded)

  override def onNodeUnblacklisted(nodeUnblacklisted: SparkListenerNodeUnblacklisted): Unit = super.onNodeUnblacklisted(nodeUnblacklisted)

  override def onNodeUnexcluded(nodeUnexcluded: SparkListenerNodeUnexcluded): Unit = super.onNodeUnexcluded(nodeUnexcluded)

  override def onUnschedulableTaskSetAdded(unschedulableTaskSetAdded: SparkListenerUnschedulableTaskSetAdded): Unit = super.onUnschedulableTaskSetAdded(unschedulableTaskSetAdded)

  override def onUnschedulableTaskSetRemoved(unschedulableTaskSetRemoved: SparkListenerUnschedulableTaskSetRemoved): Unit = super.onUnschedulableTaskSetRemoved(unschedulableTaskSetRemoved)

  override def onBlockUpdated(blockUpdated: SparkListenerBlockUpdated): Unit = super.onBlockUpdated(blockUpdated)

  override def onSpeculativeTaskSubmitted(speculativeTask: SparkListenerSpeculativeTaskSubmitted): Unit = super.onSpeculativeTaskSubmitted(speculativeTask)

  override def onOtherEvent(event: SparkListenerEvent): Unit = super.onOtherEvent(event)

  override def onResourceProfileAdded(event: SparkListenerResourceProfileAdded): Unit = super.onResourceProfileAdded(event)
}
