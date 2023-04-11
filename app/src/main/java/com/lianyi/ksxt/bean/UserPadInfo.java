package com.lianyi.ksxt.bean;


public class UserPadInfo {
    public String roomName = "";//"考场名称"
    public String roomId = "";//"考场id"
    public String studentName = ""; //"学生姓名"
    public String subjectName = ""; //考试科目,
    public String question = ""; //"考试题"
    public String seatSeg = "";//"考桌编号"
    public String studentId = ""; //考生id,
    public String studentCode = ""; //"准考证号"
    public String schoolName = ""; //"学校名称"
    public String realBeginTime = ""; //"考试真正开始时间"
    public int examMinute; //考试时长 分钟
    public int confirm; // 学生信息确认状态 0未确认 1已确认,
    public int examinationStatus;// 考生考试状态 0 未开始 1进行中 2已结束,
    public String sceneId; // 场次id
}
