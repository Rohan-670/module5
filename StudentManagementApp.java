package com.example.studentmanagement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/** Swing frontend for the MySQL-backed Student Management System. */
public class StudentManagementApp extends JFrame {
    private final StudentDAO studentDAO = new StudentDAO();
    private final JTextField idField = new JTextField(8);
    private final JTextField nameField = new JTextField(22);
    private final JTextField emailField = new JTextField(22);
    private final JTextField phoneField = new JTextField(22);
    private final JTextField courseField = new JTextField(22);
    private final JTextField searchField = new JTextField(20);
    private final JLabel statusLabel = new JLabel("Ready");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Full Name", "Email", "Phone", "Course"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable studentsTable = new JTable(tableModel);

    public StudentManagementApp() {
        super("Student Management System | Java + MySQL");
        buildUI();
        attachEvents();
        setMinimumSize(new Dimension(900, 580));
        setSize(1120, 700);
        setLocationRelativeTo(null);
        refreshStudents("");
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(28, 53, 87));
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel title = new JLabel("Student Management System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel subtitle = new JLabel("Java Swing frontend  •  JDBC  •  MySQL database");
        subtitle.setForeground(new Color(220, 231, 245));
        JPanel titleStack = new JPanel(new BorderLayout(0, 5));
        titleStack.setOpaque(false);
        titleStack.add(title, BorderLayout.NORTH);
        titleStack.add(subtitle, BorderLayout.SOUTH);
        header.add(titleStack, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 12));
        center.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        center.add(buildForm(), BorderLayout.WEST);
        center.add(buildTablePanel(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(4, 18, 12, 18));
        statusLabel.setForeground(new Color(65, 81, 100));
        footer.add(statusLabel, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Student details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        form.setPreferredSize(new Dimension(330, 360));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 5, 6, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;
        addFormRow(form, c, "ID (select from table)", idField);
        idField.setEditable(false);
        addFormRow(form, c, "Full name *", nameField);
        addFormRow(form, c, "Email *", emailField);
        addFormRow(form, c, "Phone", phoneField);
        addFormRow(form, c, "Course *", courseField);

        JPanel buttons = new JPanel(new GridBagLayout());
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearForm());
        GridBagConstraints b = new GridBagConstraints();
        b.insets = new Insets(4, 4, 4, 4);
        b.fill = GridBagConstraints.HORIZONTAL;
        b.weightx = 1;
        b.gridx = 0; b.gridy = 0; buttons.add(addButton, b);
        b.gridx = 1; buttons.add(updateButton, b);
        b.gridx = 0; b.gridy = 1; buttons.add(deleteButton, b);
        b.gridx = 1; buttons.add(clearButton, b);
        c.gridx = 0; c.gridy++; c.gridwidth = 2; c.weightx = 1;
        form.add(buttons, c);
        c.gridy++; c.weighty = 1; form.add(new JPanel(), c);
        return form;
    }

    private void addFormRow(JPanel panel, GridBagConstraints c, String labelText, JTextField field) {
        c.gridx = 0; c.gridwidth = 2; c.weightx = 1;
        panel.add(new JLabel(labelText), c);
        c.gridy++;
        panel.add(field, c);
        c.gridy++;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Records in MySQL"));
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Show all");
        JPanel searchButtons = new JPanel();
        searchButtons.add(searchButton);
        searchButtons.add(refreshButton);
        searchPanel.add(searchButtons, BorderLayout.EAST);
        searchButton.addActionListener(e -> refreshStudents(searchField.getText()));
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            refreshStudents("");
        });
        searchField.addActionListener(e -> refreshStudents(searchField.getText()));

        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.setRowHeight(25);
        studentsTable.setAutoCreateRowSorter(true);
        studentsTable.getTableHeader().setReorderingAllowed(false);
        studentsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentsTable.getSelectedRow() >= 0) {
                int viewRow = studentsTable.getSelectedRow();
                int row = studentsTable.convertRowIndexToModel(viewRow);
                idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
                nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                emailField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                Object phone = tableModel.getValueAt(row, 3);
                phoneField.setText(phone == null ? "" : String.valueOf(phone));
                courseField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
            }
        });
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        return panel;
    }

    private void attachEvents() {
        // The table, form actions and search controls are wired in their UI builders.
    }

    private Student readForm() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String course = courseField.getText().trim();
        if (name.isEmpty() || email.isEmpty() || course.isEmpty()) {
            throw new IllegalArgumentException("Please fill in Full name, Email and Course.");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (!phone.isEmpty() && !phone.matches("[+()0-9 .-]{6,25}")) {
            throw new IllegalArgumentException("Phone can contain digits, spaces, +, -, . and parentheses.");
        }
        return new Student(0, name, email, phone, course);
    }

    private void addStudent() {
        try {
            Student student = readForm();
            long id = studentDAO.add(student);
            refreshStudents("");
            clearForm();
            statusLabel.setText("Added student with ID " + id + ". Total records: " + studentDAO.count());
            JOptionPane.showMessageDialog(this, "Student added successfully. ID: " + id,
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateStudent() {
        Long id = selectedId();
        if (id == null) {
            showValidationError("Select a student from the table before updating.");
            return;
        }
        try {
            boolean updated = studentDAO.update(id, readForm());
            refreshStudents(searchField.getText());
            if (updated) {
                statusLabel.setText("Updated student ID " + id + ".");
                JOptionPane.showMessageDialog(this, "Student updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "No student with that ID was found.",
                        "Not found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteStudent() {
        Long id = selectedId();
        if (id == null) {
            showValidationError("Select a student from the table before deleting.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Permanently delete student ID " + id + "?", "Confirm deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            boolean deleted = studentDAO.delete(id);
            refreshStudents("");
            clearForm();
            statusLabel.setText(deleted ? "Deleted student ID " + id + "." : "Student was not found.");
            if (!deleted) {
                JOptionPane.showMessageDialog(this, "No student with that ID was found.");
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private Long selectedId() {
        String value = idField.getText().trim();
        if (value.isEmpty()) return null;
        try { return Long.parseLong(value); }
        catch (NumberFormatException ex) { return null; }
    }

    private void refreshStudents(String searchText) {
        try {
            List<Student> students = studentDAO.findAll(searchText);
            tableModel.setRowCount(0);
            for (Student s : students) {
                tableModel.addRow(new Object[]{s.id(), s.fullName(), s.email(), s.phone(), s.course()});
            }
            statusLabel.setText("Showing " + students.size() + " record(s). Total in database: " + studentDAO.count());
        } catch (SQLException ex) {
            statusLabel.setText("Database connection unavailable. Check README setup instructions.");
            if (isShowing()) showDatabaseError(ex);
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        courseField.setText("");
        studentsTable.clearSelection();
        nameField.requestFocusInWindow();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Check input", JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(SQLException ex) {
        String message = "Could not complete the database operation.\n\n"
                + "Confirm that MySQL is running, schema.sql has been executed, and DB_USER / DB_PASSWORD are set.\n\n"
                + "MySQL message: " + ex.getMessage();
        JOptionPane.showMessageDialog(this, message, "Database error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { /* Use Swing's default theme if the system theme is unavailable. */ }
        SwingUtilities.invokeLater(() -> new StudentManagementApp().setVisible(true));
    }
}
