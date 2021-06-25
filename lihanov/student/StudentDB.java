package info.kgeorgiy.ja.lihanov.student;

import info.kgeorgiy.java.advanced.student.*;

import java.sql.Struct;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentDB implements GroupQuery {

    private static final Comparator<Student> ORDER_BY_NAME_COMPARATOR = Comparator
            .comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder())
            .thenComparing(Student::getId);

    private static <T, V> List<T> getMappedList(final List<V> students, final Function<V, ? extends T> function) {
        return students
                .stream()
                .map(function)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return getMappedList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final List<Student> students) {
        return getMappedList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(final List<Student> students) {
        return getMappedList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final List<Student> students) {
        return getMappedList(
                students,
                student -> student.getFirstName() + " " + student.getLastName()
        );
    }

    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return students
                .stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(final List<Student> students) {
        return students
                .stream()
                // :NOTE: Исправить
                .max(Comparator.comparingInt(Student::getId))
                .map(Student::getFirstName)
                .orElse("");
    }

    private static List<Student> getListSortedBy(final Collection<Student> students, final Comparator<Student> comparator) {
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        // :NOTE: Исправить
        return getListSortedBy(students, Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return getListSortedBy(students, ORDER_BY_NAME_COMPARATOR);
    }

    private static <T> List<Student> findStudentsBy(
            final Collection<Student> students,
            final T name,
            final Function<Student, T> function) {
        return students
                .stream()
                .filter(equalsPredicate(name, function))
                .sorted(ORDER_BY_NAME_COMPARATOR)
                .collect(Collectors.toList());
    }

    private static <T, V> Predicate<T> equalsPredicate(final V value, final Function<T, V> function) {
        return student -> value.equals(function.apply(student));
    }

    // :NOTE: Дублирование
    @Override
    public List<Student> findStudentsByFirstName(final Collection<Student> students, final String name) {
        return findStudentsBy(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(final Collection<Student> students, final String name) {
        return findStudentsBy(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students, final GroupName group) {
        return findStudentsBy(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final GroupName group) {
        return students
                .stream()
                .filter(student -> group.equals(student.getGroup()))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)
                ));
    }

    private static List<Group> getGroupsBy(final Collection<Student> students, final Comparator<Student> comparator) {
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream()
                .map(entry -> new Group(
                        entry.getKey(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(final Collection<Student> students) {
        return getGroupsBy(students, ORDER_BY_NAME_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(final Collection<Student> students) {
        return getGroupsBy(students, Comparator.comparingInt(Student::getId));
    }

    private GroupName getLargestGroupBy(
            final Collection<Student> students,
            final Function<Group, Long> functionCount,
            final Comparator<Group> comparator
    ) {
        return getGroupsBy(students, ORDER_BY_NAME_COMPARATOR)
                .stream()
                .max(Comparator.comparing(functionCount).thenComparing(comparator))
                .map(Group::getName)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroup(final Collection<Student> students) {
        return getLargestGroupBy(
                students,
                group -> (long) group.getStudents().size(),
                Comparator.comparing(Group::getName)
        );
    }

    @Override
    public GroupName getLargestGroupFirstName(final Collection<Student> students) {
        return getLargestGroupBy(
                students,
                group -> group.getStudents()
                        .stream()
                        .map(Student::getFirstName)
                        .distinct()
                        .count(),
                Comparator.comparing(Group::getName, Comparator.reverseOrder()));
    }
}
