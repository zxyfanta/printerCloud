package com.printercloud.admin.view.swing.panel;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 文件管理面板
 */
public class FileManagementPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(FileManagementPanel.class);
    
    // UI组件
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JButton refreshButton;
    private JButton downloadButton;
    private JButton deleteButton;
    private JButton previewButton;
    
    public FileManagementPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 搜索框
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "搜索文件名...");
        
        // 类型筛选
        typeFilter = new JComboBox<>(new String[]{
            "全部类型", "PDF", "图片", "文档", "其他"
        });
        
        // 创建表格
        createTable();
        
        // 统计标签
        totalLabel = new JLabel("总计: 0 个文件");
        totalLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 按钮
        refreshButton = new JButton("刷新");
        downloadButton = new JButton("下载");
        deleteButton = new JButton("删除");
        previewButton = new JButton("预览");
        
        // 设置按钮状态
        updateButtonStates();
    }
    
    /**
     * 创建表格
     */
    private void createTable() {
        String[] columnNames = {
            "文件名", "类型", "大小", "上传者", "上传时间", "下载次数"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setRowHeight(25);
        fileTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        fileTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 设置列宽
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(250); // 文件名
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // 类型
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 大小
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 上传者
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(150); // 上传时间
        fileTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 下载次数
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[]10[]10[grow]10[]"));
        
        // 顶部工具栏
        JPanel toolPanel = new JPanel(new MigLayout("insets 0", "[]10[]10[]push[]", "[]"));
        
        toolPanel.add(new JLabel("搜索:"));
        toolPanel.add(searchField);
        toolPanel.add(typeFilter);
        toolPanel.add(refreshButton);
        
        add(toolPanel, "growx, wrap");
        
        // 统计信息
        add(totalLabel, "wrap");
        
        // 表格
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("文件列表"));
        add(scrollPane, "grow, wrap");
        
        // 底部按钮
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[]10[]10[]", "[]"));
        
        buttonPanel.add(previewButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(deleteButton);
        
        add(buttonPanel, "left");
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 刷新按钮
        refreshButton.addActionListener(e -> loadData());
        
        // 搜索框
        searchField.addActionListener(e -> search());
        
        // 类型筛选
        typeFilter.addActionListener(e -> search());
        
        // 表格选择事件
        fileTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // 操作按钮
        previewButton.addActionListener(e -> previewFile());
        downloadButton.addActionListener(e -> downloadFile());
        deleteButton.addActionListener(e -> deleteFile());
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // 模拟网络延迟
                Thread.sleep(500);
                return null;
            }
            
            @Override
            protected void done() {
                // 模拟数据
                tableModel.setRowCount(0);
                
                Object[][] sampleData = {
                    {"报告.pdf", "PDF", "2.5 MB", "张三", "2024-01-15 10:30", "15"},
                    {"图片.jpg", "图片", "1.2 MB", "李四", "2024-01-15 09:45", "8"},
                    {"文档.docx", "文档", "856 KB", "王五", "2024-01-14 16:20", "23"},
                    {"表格.xlsx", "文档", "445 KB", "赵六", "2024-01-14 14:15", "12"},
                    {"演示.pptx", "文档", "3.2 MB", "钱七", "2024-01-13 11:30", "6"}
                };
                
                for (Object[] row : sampleData) {
                    tableModel.addRow(row);
                }
                
                totalLabel.setText("总计: " + sampleData.length + " 个文件");
                updateButtonStates();
                
                logger.info("文件列表数据加载完成");
            }
        };
        
        worker.execute();
    }
    
    /**
     * 搜索
     */
    private void search() {
        logger.info("搜索文件: {}, 类型: {}", searchField.getText(), typeFilter.getSelectedItem());
        loadData(); // 重新加载数据
    }
    
    /**
     * 预览文件
     */
    private void previewFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow >= 0) {
            String fileName = (String) tableModel.getValueAt(selectedRow, 0);
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "文件预览", true);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(this);
            
            JLabel label = new JLabel("预览: " + fileName, SwingConstants.CENTER);
            label.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            
            dialog.add(label);
            dialog.setVisible(true);
            
            logger.info("预览文件: {}", fileName);
        }
    }
    
    /**
     * 下载文件
     */
    private void downloadFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow >= 0) {
            String fileName = (String) tableModel.getValueAt(selectedRow, 0);
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(fileName));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                // 这里执行下载逻辑
                JOptionPane.showMessageDialog(this, "文件下载完成: " + fileName);
                logger.info("下载文件: {}", fileName);
            }
        }
    }
    
    /**
     * 删除文件
     */
    private void deleteFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow >= 0) {
            String fileName = (String) tableModel.getValueAt(selectedRow, 0);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除文件 \"" + fileName + "\" 吗？\n此操作不可恢复！",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // 这里执行删除逻辑
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "文件已删除: " + fileName);
                logger.info("删除文件: {}", fileName);
                
                // 更新统计
                totalLabel.setText("总计: " + tableModel.getRowCount() + " 个文件");
                updateButtonStates();
            }
        }
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasSelection = fileTable.getSelectedRow() >= 0;
        previewButton.setEnabled(hasSelection);
        downloadButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }
    
    /**
     * 刷新数据
     */
    public void refresh() {
        logger.info("刷新文件管理数据");
        loadData();
    }
}
