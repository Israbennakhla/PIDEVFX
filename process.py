import os

directory = 'src/main/java/com/sitmypet/controllers'
for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            
            # Replace common emojis
            content = content.replace('🐾', '')
            content = content.replace('✅', '')
            content = content.replace('❌', '')
            content = content.replace('⚠️', '!')
            content = content.replace('📧', '')
            content = content.replace('📞', '')
            content = content.replace('📢', '')
            content = content.replace('📍', '')
            content = content.replace('💰', '')
            content = content.replace('📅', '')
            content = content.replace('💬', 'Repondre')
            content = content.replace('🗑️', 'Supprimer')
            content = content.replace('🗑', 'Supprimer')
            content = content.replace('✏️', 'Modifier')
            content = content.replace('✏', 'Modifier')
            content = content.replace('👤', '')
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
print('Emojis removed from all controllers')
