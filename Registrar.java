
import tester.*;

class Course {
  String name;
  Instructor prof;
  IList<Student> students;

  Course(String name, Instructor prof) {
    prof.addCourse(this);
    this.name = name;
    this.prof = prof;
    this.students = new MtList<Student>();
  }

  // accepts visitor
  <T, R> R accept(IListVisitor<T, R> f) {
    return f.visitCourse(this);
  }

  // EFFECT: adds given student to list of students in course
  void enrollStudent(Student student) {
    this.students = students.add(student);

  }
}

class Instructor {
  String name;
  IList<Course> courses;

  Instructor(String name) {
    this.name = name;
    this.courses = new MtList<Course>();
  }

  // determines whether the given student is in more than one of this instructor's
  // courses
  boolean dejavu(Student student) {
    return this.courses.checkStudent(new CourseVisitor(student.id));
  }

  // EFFECT: adds given course to current instructor's list of courses
  void addCourse(Course course) {
    this.courses = this.courses.add(course);
  }
}

class Student {
  String name;
  int id;
  IList<Course> courses;

  Student(String name, int id) {
    this.name = name;
    this.id = id;
    this.courses = new MtList<Course>();

  }

  // EFFECT: enrolls a student in given course
  void enroll(Course c) {
    this.courses = courses.add(c);
    c.enrollStudent(this);
  }

  // determines whether the given student is in any of the same classes as this
  // student
  boolean classmates(Student student) {
    return courses.orMap(new CourseVisitor(student.id));
  }

  // checks if current student is same as given student by comparing id numbers
  boolean checkIdNum(int otherId) {
    return this.id == otherId;
  }

  // accepts visitor
  <T, R> R accept(IListVisitor<T, R> f) {
    return f.visitStudent(this);
  }

}

interface IFunc<T, R> {
  // apply an operation to the given item
  R apply(T t);
}

interface IListVisitor<T, R> extends IFunc<T, R> {
  // visits a given student and returns R
  R visitStudent(Student student);

  // visits a given course and returns R
  R visitCourse(Course course);
}

class StudentVisitor implements IListVisitor<Student, Boolean> {
  int id;

  StudentVisitor(int id) {
    this.id = id;
  }

  // applies accept to this
  public Boolean apply(Student t) {
    return t.accept(this);
  }

  // checks if this id matches the id of the given student
  public Boolean visitStudent(Student student) {
    return student.checkIdNum(this.id);
  }

  // visits course
  public Boolean visitCourse(Course course) {
    return null;
  }
}

class CourseVisitor implements IListVisitor<Course, Boolean> {
  int id;

  CourseVisitor(int id) {
    this.id = id;
  }

  // visits student
  public Boolean visitStudent(Student student) {
    return null;
  }

  // is the given id is in given courses list of students?
  public Boolean visitCourse(Course course) {
    return course.students.orMap(new StudentVisitor(this.id));
  }

  // applies accept to this
  public Boolean apply(Course t) {
    return t.accept(this);
  }
}

interface IList<T> {
  // checks through list of T with given function f
  boolean orMap(IListVisitor<T, Boolean> f);

  // checks if student is in more than one of instructor's classes
  boolean checkStudent(IListVisitor<T, Boolean> f);

  // adds given object to front of list T
  IList<T> add(T other);

}

class MtList<T> implements IList<T> {

  // checks through list of T with given function f
  public boolean orMap(IListVisitor<T, Boolean> f) {
    return false;
  }

  // checks if student is in more than one of instructor's classes
  public boolean checkStudent(IListVisitor<T, Boolean> f) {
    return false;
  }

  // adds given object to front of list T
  public IList<T> add(T other) {
    return new ConsList<T>(other, this);
  }
}

class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // checks through list of T with given function f
  public boolean orMap(IListVisitor<T, Boolean> f) {
    return f.apply(this.first) || this.rest.orMap(f);
  }

  // checks if student is in more than one of instructor's classes
  public boolean checkStudent(IListVisitor<T, Boolean> f) {
    if (f.apply(this.first)) {
      return this.rest.orMap(f);
    }
    return this.rest.checkStudent(f);
  }

  // adds given object to front of list T
  public IList<T> add(T other) {
    return new ConsList<T>(other, this);
  }
}

class ExamplesRegistrar {
  Student isabella = new Student("Isabella", 0001);
  Student sean = new Student("Sean", 0002);
  Student abby = new Student("Abby", 0003);
  Student bob = new Student("Bob", 0004);
  Student carly = new Student("Carly", 0005);

  Instructor stone = new Instructor("Stone");
  Instructor moll = new Instructor("Moll");
  Course micro = new Course("Micro", this.stone);
  Course calc = new Course("Calculus", this.moll);
  Course macro = new Course("Macro", this.stone);
  Course econ = new Course("Econ", this.stone);

  // EFFECT: brings data back to its initial condition
  void initData() {
    micro.students = new MtList<Student>();
    macro.students = new MtList<Student>();
    calc.students = new MtList<Student>();
    econ.students = new MtList<Student>();
    isabella.courses = new MtList<Course>();
    isabella.enroll(calc);
    sean.courses = new MtList<Course>();
    sean.enroll(macro);
    sean.enroll(micro);
    abby.courses = new MtList<Course>();
    abby.enroll(econ);
    bob.courses = new MtList<Course>();
    bob.enroll(econ);
    carly.courses = new MtList<Course>();
    carly.enroll(econ);
    carly.enroll(micro);
  }

  void testEnroll(Tester t) {
    this.initData();
    t.checkExpect(isabella.courses, new ConsList<Course>(calc, new MtList<Course>()));
    t.checkExpect(sean.courses,
        new ConsList<Course>(micro, new ConsList<Course>(macro, new MtList<Course>())));
  }

  void testcheckIdNum(Tester t) {
    this.initData();
    t.checkExpect(abby.checkIdNum(0003), true);
    t.checkExpect(carly.checkIdNum(0001), false);
  }

  void testAcceptCourse(Tester t) {
    this.initData();
    t.checkExpect(econ.accept(new CourseVisitor(001)), false);
    t.checkExpect(calc.accept(new CourseVisitor(001)), true);
    t.checkExpect(carly.accept(new CourseVisitor(001)), null);
  }

  void testAcceptStudent(Tester t) {
    this.initData();
    t.checkExpect(econ.accept(new StudentVisitor(001)), null);
    t.checkExpect(isabella.accept(new StudentVisitor(001)), true);
    t.checkExpect(sean.accept(new StudentVisitor(001)), false);
  }

  void testClassmates(Tester t) {
    this.initData();
    t.checkExpect(isabella.classmates(sean), false);
    t.checkExpect(bob.classmates(carly), true);
  }

  void testAddCourse(Tester t) {
    moll.addCourse(econ);
    t.checkExpect(moll.courses,
        new ConsList<Course>(econ, new ConsList<Course>(calc, new MtList<Course>())));
  }

  void testDejaVu(Tester t) {
    this.initData();
    t.checkExpect(moll.dejavu(isabella), false);
    t.checkExpect(stone.dejavu(sean), true);
  }

  void testApply(Tester t) {
    this.initData();
    t.checkExpect(new CourseVisitor(001).apply(calc), true);
    t.checkExpect(new CourseVisitor(002).apply(calc), false);
  }

  void testEnrollStudent(Tester t) {
    this.initData();
    econ.students = new MtList<Student>();
    calc.enrollStudent(sean);
    t.checkExpect(calc.students,
        new ConsList<Student>(sean, new ConsList<Student>(isabella, new MtList<Student>())));
    Course discrete = new Course("Discrete", this.stone);
    discrete.enrollStudent(sean);
    t.checkExpect(discrete.students, new ConsList<Student>(sean, new MtList<Student>()));
  }

  void testCheckStudent(Tester t) {
    this.initData();
    t.checkExpect(stone.courses.checkStudent(new CourseVisitor(0005)), true);
    t.checkExpect(stone.courses.checkStudent(new CourseVisitor(0001)), false);
    t.checkExpect(stone.courses.checkStudent(new CourseVisitor(0004)), false);
  }

  void testVisitCourse(Tester t) {
    this.initData();
    t.checkExpect(new CourseVisitor(0001).visitCourse(calc), true);
    t.checkExpect(new CourseVisitor(0002).visitCourse(calc), false);
  }

  void testOrMap(Tester t) {
    this.initData();
    t.checkExpect(stone.courses.orMap(new CourseVisitor(0005)), true);
    t.checkExpect(stone.courses.orMap(new CourseVisitor(0001)), false);
    t.checkExpect(stone.courses.orMap(new CourseVisitor(0004)), true);
    t.checkExpect(moll.courses.orMap(new CourseVisitor(0001)), true);
    t.checkExpect(moll.courses.orMap(new CourseVisitor(0002)), false);
  }

}
