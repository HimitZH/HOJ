package top.hcode.hoj.schedule;

public interface ScheduleService {
    void deleteAvatar();

    void deleteTestCase();

    void deleteContestPrintText();

    void getOjContestsList();

    void getCodeforcesRating();

    void deleteUserSession();

    void syncNoticeToRecentHalfYearUser();

    void check20MPendingSubmission();

    void checkUnHandleGroupProblemApplyProgress();
}
