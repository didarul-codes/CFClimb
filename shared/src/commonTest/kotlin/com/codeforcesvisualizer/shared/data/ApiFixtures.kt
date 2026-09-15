package com.codeforcesvisualizer.shared.data

/**
 * Codeforces API responses recorded on 2026-09-15 and trimmed to a few entries. Unknown fields
 * (points, testset, handle, ...) are kept on purpose: parsing must ignore them.
 */
internal object ApiFixtures {

    const val USER_INFO = """{"status":"OK","result":[{"lastName":"Korotkevich","country":"Belarus","lastOnlineTimeSeconds":1789449026,"city":"Gomel","rating":3301,"friendOfCount":90802,"titlePhoto":"https://userpic.codeforces.org/422/title/50a270ed4a722867.jpg","handle":"tourist","avatar":"https://userpic.codeforces.org/422/avatar/2b5dbe87f0d859a2.jpg","firstName":"Gennady","contribution":112,"organization":"ITMO University","rank":"legendary grandmaster","maxRating":4009,"registrationTimeSeconds":1265987288,"maxRank":"tourist"}]}"""

    const val USER_RATING = """{"status":"OK","result":[{"contestId":2,"contestName":"Codeforces Beta Round 2","handle":"tourist","rank":14,"ratingUpdateTimeSeconds":1267124400,"oldRating":0,"newRating":1602},{"contestId":8,"contestName":"Codeforces Beta Round 8","handle":"tourist","rank":5,"ratingUpdateTimeSeconds":1270748700,"oldRating":1602,"newRating":1764}]}"""

    const val USER_STATUS = """{"status":"OK","result":[{"id":390651990,"contestId":1401,"creationTimeSeconds":1789326747,"relativeTimeSeconds":2147483647,"problem":{"contestId":1401,"index":"F","name":"Reverse and Swap","type":"PROGRAMMING","points":2500.0,"rating":2400,"tags":["binary search","bitmasks","data structures"]},"author":{"contestId":1401,"participantId":246595267,"members":[{"handle":"tourist"}],"participantType":"PRACTICE","ghost":false,"startTimeSeconds":1598020500},"programmingLanguage":"C++23 (GCC 14-64, msys2)","verdict":"OK","testset":"TESTS","passedTestCount":62,"timeConsumedMillis":156,"memoryConsumedBytes":3686400},{"id":390648109,"contestId":1401,"creationTimeSeconds":1789325575,"relativeTimeSeconds":2147483647,"problem":{"contestId":1401,"index":"F","name":"Reverse and Swap","type":"PROGRAMMING","points":2500.0,"rating":2400,"tags":["binary search","bitmasks","data structures"]},"author":{"contestId":1401,"participantId":246595267,"members":[{"handle":"tourist"}],"participantType":"PRACTICE","ghost":false,"startTimeSeconds":1598020500},"programmingLanguage":"C++23 (GCC 14-64, msys2)","verdict":"WRONG_ANSWER","testset":"TESTS","passedTestCount":2,"timeConsumedMillis":31,"memoryConsumedBytes":0}]}"""

    /**
     * Shaped like a recorded submission, edited to cover documented optional fields: the verdict
     * is absent while judging, and acmsguru problems have no contestId.
     */
    const val USER_STATUS_OPTIONAL_FIELDS = """{"status":"OK","result":[{"id":1,"creationTimeSeconds":1789326747,"relativeTimeSeconds":2147483647,"problem":{"problemsetName":"acmsguru","index":"100","name":"A+B","type":"PROGRAMMING","tags":[]},"author":{"members":[{"handle":"tourist"}],"ghost":false},"programmingLanguage":"C++23 (GCC 14-64, msys2)","testset":"TESTS","passedTestCount":0,"timeConsumedMillis":0,"memoryConsumedBytes":0}]}"""

    const val CONTEST_LIST = """{"status":"OK","result":[{"id":2261,"name":"Codeforces Round (Div. 1 + Div. 2)","type":"CF","phase":"BEFORE","frozen":false,"durationSeconds":10800,"startTimeSeconds":1792247700,"relativeTimeSeconds":-2788463},{"id":2264,"name":"Codeforces Round 1121 (Div. 2)","type":"CF","phase":"FINISHED","frozen":false,"durationSeconds":7200,"startTimeSeconds":1789319100,"relativeTimeSeconds":140137}]}"""

    /** Returned with HTTP 400. */
    const val USER_STATUS_NOT_FOUND = """{"status":"FAILED","comment":"handle: User with handle zz_no_such_handle_zz not found"}"""
}
