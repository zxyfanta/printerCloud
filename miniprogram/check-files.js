// 检查小程序文件完整性
const fs = require('fs');
const path = require('path');

// 必需的文件列表
const requiredFiles = [
  'app.js',
  'app.json',
  'app.wxss',
  'project.config.json',
  'sitemap.json',
  
  // 页面文件
  'pages/index/index.js',
  'pages/index/index.wxml',
  'pages/index/index.wxss',
  'pages/index/index.json',
  
  'pages/upload/upload.js',
  'pages/upload/upload.wxml',
  'pages/upload/upload.wxss',
  
  'pages/config/config.js',
  'pages/config/config.wxml',
  'pages/config/config.wxss',
  
  'pages/confirm/confirm.js',
  'pages/confirm/confirm.wxml',
  'pages/confirm/confirm.wxss',
  
  'pages/upload-server/upload-server.js',
  'pages/upload-server/upload-server.wxml',
  'pages/upload-server/upload-server.wxss',
  
  'pages/orders/orders.js',
  'pages/orders/orders.wxml',
  'pages/orders/orders.wxss',
  
  'pages/order-detail/order-detail.js',
  'pages/order-detail/order-detail.wxml',
  'pages/order-detail/order-detail.wxss',
  
  'pages/profile/profile.js',
  'pages/profile/profile.wxml',
  'pages/profile/profile.wxss',
  
  'pages/payment/payment.js',
  'pages/payment/payment.wxml'
];

console.log('检查小程序文件完整性...\n');

let missingFiles = [];
let existingFiles = [];

requiredFiles.forEach(file => {
  if (fs.existsSync(file)) {
    existingFiles.push(file);
    console.log(`✅ ${file}`);
  } else {
    missingFiles.push(file);
    console.log(`❌ ${file} - 文件不存在`);
  }
});

console.log(`\n📊 统计信息:`);
console.log(`   存在的文件: ${existingFiles.length}`);
console.log(`   缺失的文件: ${missingFiles.length}`);
console.log(`   总文件数: ${requiredFiles.length}`);

if (missingFiles.length > 0) {
  console.log(`\n⚠️  缺失的文件:`);
  missingFiles.forEach(file => {
    console.log(`   - ${file}`);
  });
} else {
  console.log(`\n🎉 所有必需文件都存在！`);
}

// 检查app.json中的页面配置
try {
  const appJson = JSON.parse(fs.readFileSync('app.json', 'utf8'));
  console.log(`\n📄 app.json 页面配置:`);
  appJson.pages.forEach(page => {
    console.log(`   - ${page}`);
  });
} catch (error) {
  console.log(`\n❌ 读取app.json失败: ${error.message}`);
}
