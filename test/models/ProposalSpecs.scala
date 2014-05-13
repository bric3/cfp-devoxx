/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Association du Paris Java User Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package models

import play.api.test.{WithApplication, FakeApplication, PlaySpecification}

/**
 * Play uses Specs2 for testing.
 *
 * Author: nicolas
 * Created: 14/01/2014 12:17
 */
class ProposalSpecs extends PlaySpecification {
  // Test with a remote server to check for performance issues
  val remoteRedisTestServer = Map(
    "redis.host" -> "localhost"
    , "redis.port" -> "6363"
    //,"redis.password" -> "test_540240240230423042440230"
  )
  val appWithTestRedis = FakeApplication(additionalConfiguration = remoteRedisTestServer)

  "retrieve a simple proposal" in new WithApplication(FakeApplication()) {
    val Some(proposal) = Proposal.findById("WTU-699")
    proposal.id must equalTo("WTU-699")
  }

  "correctly update the track when we change one proposal track to another" in new WithApplication(FakeApplication()) {

    val newProposal = new Proposal(id="TST-000", "DV", "fr", "title","123mainspeaker",
                                  None,Nil,
                                  ProposalType.CONF, "audience",
                                  "summary","generated by tests",
                                  ProposalState.UNKNOWN, false,
                                  Track.AGILITE, "d1", false)

    Proposal.save("123", newProposal, ProposalState.SUBMITTED)

    val Some(proposal) = Proposal.findById("TST-000")
    proposal.id must equalTo("TST-000")
    proposal.track must equalTo(Track.AGILITE)
    proposal.state must equalTo(ProposalState.SUBMITTED)

    // Get total of votes for Agilite
    val totalSubmitted = Proposal.totalSubmittedByTrack()
    val Some(totalAgilite) = totalSubmitted.find(p=>p._1==Track.AGILITE)
    val Some(totalFuture) = totalSubmitted.find(p=>p._1==Track.FUTURE)
    val totalAg = totalAgilite._2
    val totalFu = totalFuture._2


    Proposal.changeTrack("1234", proposal.copy(track = Track.FUTURE))
    val totalSubmitted2 = Proposal.totalSubmittedByTrack()
    val Some(totalAgilite2) = totalSubmitted2.find(p=>p._1==Track.AGILITE)
    val Some(totalFuture2) = totalSubmitted2.find(p=>p._1==Track.FUTURE)
    totalAgilite2._2 must equalTo(totalAg - 1)
    totalFuture2._2 must equalTo(totalFu +1)

    Proposal.changeTrack("1234", proposal.copy(track = Track.FUTURE))
    val totalSubmitted3 = Proposal.totalSubmittedByTrack()
    val Some(totalAgilite3) = totalSubmitted3.find(p=>p._1==Track.AGILITE)
    val Some(totalFuture3) = totalSubmitted3.find(p=>p._1==Track.FUTURE)
    totalAgilite3._2 must equalTo(totalAg - 1)
    totalFuture3._2 must equalTo(totalFu +1)


    Proposal.changeTrack("1234", proposal.copy(track = Track.JAVA))
    val totalSubmitted4 = Proposal.totalSubmittedByTrack()
    val Some(totalAgilite4) = totalSubmitted4.find(p=>p._1==Track.AGILITE)
    val Some(totalFuture4) = totalSubmitted4.find(p=>p._1==Track.FUTURE)
    totalAgilite4._2 must equalTo(totalAg - 1)
    totalFuture4._2 must equalTo(totalFu )


    Proposal.delete("1234","TST-000")
  }
}
