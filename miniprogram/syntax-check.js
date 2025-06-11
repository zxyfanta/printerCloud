// 简单的语法检查脚本
const fs = require('fs');
const path = require('path');

// 检查JavaScript文件语法
function checkJSFile(filePath) {
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    
    // 简单的语法检查
    // 检查括号匹配
    let braceCount = 0;
    let parenCount = 0;
    let bracketCount = 0;
    
    for (let i = 0; i < content.length; i++) {
      const char = content[i];
      switch (char) {
        case '{': braceCount++; break;
        case '}': braceCount--; break;
        case '(': parenCount++; break;
        case ')': parenCount--; break;
        case '[': bracketCount++; break;
        case ']': bracketCount--; break;
      }
    }
    
    const errors = [];
    if (braceCount !== 0) errors.push(`大括号不匹配: ${braceCount}`);
    if (parenCount !== 0) errors.push(`小括号不匹配: ${parenCount}`);
    if (bracketCount !== 0) errors.push(`方括号不匹配: ${bracketCount}`);
    
    return {
      file: filePath,
      errors: errors
    };
  } catch (error) {
    return {
      file: filePath,
      errors: [`读取文件失败: ${error.message}`]
    };
  }
}

// 检查所有JS文件
const jsFiles = [
  'app.js',
  'pages/index/index.js',
  'pages/upload/upload.js',
  'pages/config/config.js',
  'pages/confirm/confirm.js',
  'pages/upload-server/upload-server.js',
  'pages/orders/orders.js',
  'pages/order-detail/order-detail.js',
  'pages/profile/profile.js'
];

console.log('开始检查JavaScript文件语法...\n');

let hasErrors = false;
jsFiles.forEach(file => {
  const result = checkJSFile(file);
  if (result.errors.length > 0) {
    hasErrors = true;
    console.log(`❌ ${file}:`);
    result.errors.forEach(error => console.log(`   ${error}`));
    console.log('');
  } else {
    console.log(`✅ ${file}: 语法检查通过`);
  }
});

if (!hasErrors) {
  console.log('\n🎉 所有JavaScript文件语法检查通过！');
} else {
  console.log('\n⚠️  发现语法错误，请修复后重试。');
}
