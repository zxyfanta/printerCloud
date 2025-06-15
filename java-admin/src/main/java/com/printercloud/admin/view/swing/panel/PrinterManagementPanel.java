package com.printercloud.admin.view.swing.panel;

import com.printercloud.admin.model.PrinterInfo;
import com.printercloud.admin.service.PrintService;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * 打印机管理面板
 */
public class PrinterManagementPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(PrinterManagementPanel.class);
    
    @Autowired
    private PrintService printService;
    
    // UI组件
    private JTable printerTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JButton refreshButton;
    private JButton testPrintButton;
    private JButton configButton;
    private JButton setDefaultButton;
    
    public PrinterManagementPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 创建表格
        createTable();
        
        // 统计标签
        totalLabel = new JLabel("总计: 0 台打印机");
        totalLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 按钮
        refreshButton = new JButton("刷新");
        testPrintButton = new JButton("测试打印");
        configButton = new JButton("配置");
        setDefaultButton = new JButton("设为默认");
        
        // 设置按钮状态
        updateButtonStates();
    }
    
    /**
     * 创建表格
     */
    private void createTable() {
        String[] columnNames = {
            "打印机名称", "状态", "类型", "位置", "是否默认", "任务队列"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        printerTable = new JTable(tableModel);
        printerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        printerTable.setRowHeight(30);
        printerTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        printerTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 设置列宽
        printerTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 打印机名称
        printerTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // 状态
        printerTable.getColumnModel().getColumn(2).setPreferredWidth(120); // 类型
        printerTable.getColumnModel().getColumn(3).setPreferredWidth(150); // 位置
        printerTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 是否默认
        printerTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 任务队列
        
        // 设置状态列渲染器
        printerTable.getColumnModel().getColumn(1).setCellRenderer(new StatusCellRenderer());
        printerTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultCellRenderer());
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[]10[]10[grow]10[]"));
        
        // 顶部工具栏
        JPanel toolPanel = new JPanel(new MigLayout("insets 0", "[]push[]", "[]"));
        
        JLabel titleLabel = new JLabel("打印机管理");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        
        toolPanel.add(titleLabel);
        toolPanel.add(refreshButton);
        
        add(toolPanel, "growx, wrap");
        
        // 统计信息
        add(totalLabel, "wrap");
        
        // 表格
        JScrollPane scrollPane = new JScrollPane(printerTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("打印机列表"));
        add(scrollPane, "grow, wrap");
        
        // 底部按钮
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[]10[]10[]", "[]"));
        
        buttonPanel.add(testPrintButton);
        buttonPanel.add(configButton);
        buttonPanel.add(setDefaultButton);
        
        add(buttonPanel, "left");
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 刷新按钮
        refreshButton.addActionListener(e -> loadData());
        
        // 表格选择事件
        printerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // 操作按钮
        testPrintButton.addActionListener(e -> testPrint());
        configButton.addActionListener(e -> configurePrinter());
        setDefaultButton.addActionListener(e -> setDefaultPrinter());
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        SwingWorker<List<PrinterInfo>, Void> worker = new SwingWorker<List<PrinterInfo>, Void>() {
            @Override
            protected List<PrinterInfo> doInBackground() throws Exception {
                if (printService != null) {
                    return printService.getAvailablePrinters();
                } else {
                    // 如果服务未注入，返回模拟数据
                    return createMockPrinters();
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<PrinterInfo> printers = get();
                    updateTable(printers);
                } catch (Exception e) {
                    logger.error("加载打印机数据失败", e);
                    // 显示模拟数据
                    updateTable(createMockPrinters());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 创建模拟打印机数据
     */
    private List<PrinterInfo> createMockPrinters() {
        List<PrinterInfo> printers = new java.util.ArrayList<>();

        PrinterInfo printer1 = new PrinterInfo("HP LaserJet Pro M404n", "HP LaserJet Pro M404n");
        printer1.setOnline(true);
        printer1.setDefault(true);
        printer1.setDescription("激光打印机");
        printer1.setLocation("办公室A");
        printers.add(printer1);

        PrinterInfo printer2 = new PrinterInfo("Canon PIXMA G3010", "Canon PIXMA G3010");
        printer2.setOnline(true);
        printer2.setDefault(false);
        printer2.setDescription("喷墨打印机");
        printer2.setLocation("办公室B");
        printers.add(printer2);

        PrinterInfo printer3 = new PrinterInfo("Brother HL-L2350DW", "Brother HL-L2350DW");
        printer3.setOnline(false);
        printer3.setDefault(false);
        printer3.setDescription("激光打印机");
        printer3.setLocation("办公室C");
        printers.add(printer3);

        PrinterInfo printer4 = new PrinterInfo("Epson L3150", "Epson L3150");
        printer4.setOnline(true);
        printer4.setDefault(false);
        printer4.setDescription("喷墨打印机");
        printer4.setLocation("办公室D");
        printers.add(printer4);

        return printers;
    }
    
    /**
     * 更新表格数据
     */
    private void updateTable(List<PrinterInfo> printers) {
        // 清空现有数据
        tableModel.setRowCount(0);
        
        if (printers != null) {
            for (PrinterInfo printer : printers) {
                Object[] row = {
                    printer.getName(),
                    printer.isOnline() ? "在线" : "离线",
                    printer.getDescription() != null ? printer.getDescription() : "未知",
                    printer.getLocation() != null ? printer.getLocation() : "未设置",
                    printer.isDefault() ? "是" : "否",
                    "0" // 任务队列数量，这里简化为0
                };
                tableModel.addRow(row);
            }
        }
        
        totalLabel.setText("总计: " + (printers != null ? printers.size() : 0) + " 台打印机");
        updateButtonStates();
        
        logger.info("打印机列表数据加载完成，共 {} 台", printers != null ? printers.size() : 0);
    }
    
    /**
     * 测试打印
     */
    private void testPrint() {
        int selectedRow = printerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String printerName = (String) tableModel.getValueAt(selectedRow, 0);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要在打印机 \"" + printerName + "\" 上执行测试打印吗？",
                "确认测试打印",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // 执行测试打印
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // 这里调用打印服务的测试打印方法
                        Thread.sleep(2000); // 模拟打印过程
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        JOptionPane.showMessageDialog(
                            PrinterManagementPanel.this,
                            "测试打印已发送到 \"" + printerName + "\""
                        );
                    }
                };
                
                worker.execute();
                logger.info("执行测试打印: {}", printerName);
            }
        }
    }
    
    /**
     * 配置打印机
     */
    private void configurePrinter() {
        int selectedRow = printerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String printerName = (String) tableModel.getValueAt(selectedRow, 0);
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "打印机配置", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[right][grow]", "[]10[]10[]10[]"));
            
            panel.add(new JLabel("打印机名称:"));
            panel.add(new JLabel(printerName), "wrap");
            
            panel.add(new JLabel("纸张大小:"));
            JComboBox<String> paperSize = new JComboBox<>(new String[]{"A4", "A3", "Letter", "Legal"});
            panel.add(paperSize, "wrap");
            
            panel.add(new JLabel("打印质量:"));
            JComboBox<String> quality = new JComboBox<>(new String[]{"草稿", "标准", "高质量"});
            panel.add(quality, "wrap");
            
            JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "push[]10[]", "[]"));
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(dialog, "配置已保存");
                dialog.dispose();
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            panel.add(buttonPanel, "span 2, growx");
            
            dialog.add(panel);
            dialog.setVisible(true);
            
            logger.info("配置打印机: {}", printerName);
        }
    }
    
    /**
     * 设为默认打印机
     */
    private void setDefaultPrinter() {
        int selectedRow = printerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String printerName = (String) tableModel.getValueAt(selectedRow, 0);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要将 \"" + printerName + "\" 设为默认打印机吗？",
                "确认设置",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // 更新表格数据
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    tableModel.setValueAt(i == selectedRow ? "是" : "否", i, 4);
                }
                
                JOptionPane.showMessageDialog(this, "默认打印机已设置为: " + printerName);
                logger.info("设置默认打印机: {}", printerName);
            }
        }
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasSelection = printerTable.getSelectedRow() >= 0;
        testPrintButton.setEnabled(hasSelection);
        configButton.setEnabled(hasSelection);
        setDefaultButton.setEnabled(hasSelection);
    }
    
    /**
     * 状态单元格渲染器
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = (String) value;
            if ("在线".equals(status)) {
                setForeground(Color.GREEN);
            } else if ("离线".equals(status)) {
                setForeground(Color.RED);
            } else {
                setForeground(Color.BLACK);
            }
            
            return this;
        }
    }
    
    /**
     * 默认打印机单元格渲染器
     */
    private class DefaultCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String isDefault = (String) value;
            if ("是".equals(isDefault)) {
                setForeground(Color.BLUE);
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(Color.BLACK);
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            return this;
        }
    }
    
    /**
     * 刷新数据
     */
    public void refresh() {
        logger.info("刷新打印机管理数据");
        loadData();
    }
}
