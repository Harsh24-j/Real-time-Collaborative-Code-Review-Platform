const fs = require('fs');
const { execSync } = require('child_process');

try {
  // Get all files modified in the HEAD commit
  const files = execSync('git diff-tree --no-commit-id --name-only -r HEAD').toString().trim().split('\n');
  
  files.forEach(f => {
    if (fs.existsSync(f)) {
      let content = fs.readFileSync(f);
      // Check for UTF-8 BOM (EF BB BF)
      if (content.length >= 3 && content[0] === 0xEF && content[1] === 0xBB && content[2] === 0xBF) {
        fs.writeFileSync(f, content.slice(3));
        console.log('Removed BOM from', f);
      }
    }
  });
  console.log('BOM cleanup complete.');
} catch (e) {
  console.error('Error:', e);
}
