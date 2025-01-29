🌐 **Choose Language | เลือกภาษา**:  
[English](#readme-in-english) | [ภาษาไทย](#readme-ภาษาไทย)

---
## README in English
# 🛠️ DynamicXMLTreeEditor & XMLMerger

**DynamicXMLTreeEditor** is a GUI-based XML file editor with a **Tree Structure** format, providing an easy-to-use interface for adding, deleting, editing values, and supporting Undo/Redo functionality. Additionally, **XMLMerger** helps merge XML files from a folder into a larger XML structure conveniently.

---

## 🌟 Key Features
✅ **Tree Structure:** Displays the XML file structure in a Tree format  
✅ **Edit XML Data:** Easily add, delete, and modify Field Names and Values  
✅ **Undo/Redo:** Supports reverting and recovering changes  
✅ **Copy Fields:** Duplicate selected nodes (only permitted fields)  
✅ **Merge XML:** Combine XML files from a folder into a main XML structure  
✅ **Load & Save Files:** Supports opening and saving XML files  
✅ **User-friendly GUI:** Designed with Swing, providing comprehensive function buttons

---

## 📝 How to Use

### 📌 Using DynamicXMLTreeEditor
1️⃣ Run via direct code execution:
```bash
javac DynamicXMLTreeEditor.java
java DynamicXMLTreeEditor
```

### 📌 Using XMLMerger
1️⃣ Run via direct code execution (Specify paths for XML Structure, XML to Merge, and Save Path):
```bash
javac XMLMerger.java
java XMLMerger
```
2️⃣ Use via GUI (Refer to line 150 in the code)  
3️⃣ Run via application file: `src/application/XMLMerger.exe`

---

## 📂 Example of Supported XML

```xml
<Root>
    <Person>
        <Name>John Doe</Name>
        <Age>30</Age>
        <Address>
            <Street>Main St</Street>
            <City>New York</City>
        </Address>
    </Person>
</Root>
```

🔹 **Tree Structure in the Program:**
```
Root  
└── Person  
    ├── Name  
    ├── Age  
    └── Address  
        ├── Street  
        └── City  
```

---

## 💻 System Requirements
- **Java Runtime Environment (JRE) 17+**
- Operating System: Windows, macOS, or Linux

---

© 2025 **Phumsith Insakul**. All rights reserved.

💬 **Report issues or suggestions:** [GitHub Issues](https://github.com/username/DynamicXMLTreeEditor/issues) 🚀

---
## README ภาษาไทย
# 🛠️ DynamicXMLTreeEditor & XMLMerger

**DynamicXMLTreeEditor** เป็นโปรแกรมสำหรับแก้ไขไฟล์ XML ในรูปแบบ **Tree Structure** พร้อม GUI ที่ใช้งานง่าย รองรับการเพิ่ม ลบ แก้ไขค่า ทำสำเนา Undo/Redo และการจัดการไฟล์ XML นอกจากนี้ยังมี **XMLMerger** ที่ช่วยรวมไฟล์ XML จากโฟลเดอร์ไปยังโครงสร้าง XML ขนาดใหญ่ได้อย่างสะดวก

---

## 🌟 ฟีเจอร์หลัก
✅ **Tree Structure:** แสดงโครงสร้างไฟล์ XML ในรูปแบบต้นไม้  
✅ **แก้ไขข้อมูล XML:** เพิ่ม ลบ และแก้ไข Field Name และ Value ได้ง่าย  
✅ **Undo/Redo:** รองรับการย้อนกลับและกู้คืนการเปลี่ยนแปลง  
✅ **ทำสำเนาฟิลด์:** คัดลอกโหนดที่เลือกได้ (เฉพาะฟิลด์ที่อนุญาต)  
✅ **Merge XML:** รวมไฟล์ XML จากโฟลเดอร์เข้าสู่โครงสร้าง XML หลัก  
✅ **โหลดและบันทึกไฟล์:** รองรับการเปิดและบันทึกไฟล์ XML  
✅ **GUI ใช้งานง่าย:** ออกแบบด้วย Swing พร้อมปุ่มฟังก์ชันครบถ้วน

---

## 📝 วิธีใช้งาน

### 📌 การใช้งาน DynamicXMLTreeEditor
1️⃣ รันผ่านโค้ดโดยตรง
```bash
javac DynamicXMLTreeEditor.java
java DynamicXMLTreeEditor
```

### 📌 การใช้งาน XMLMerger
1️⃣ ใช้งานผ่านการรันโค้ดโดยตรง (กำหนด Path ของ XML Structure, XML to Merge และ Save Path)
```bash
javac XMLMerger.java
java XMLMerger
```
2️⃣ ใช้งานผ่าน GUI (ดูที่บรรทัด 150 ของโค้ด)  
3️⃣ ใช้งานผ่านไฟล์แอปพลิเคชัน: `src/application/XMLMerger.exe`

---

## 📂 ตัวอย่าง XML ที่รองรับ

```xml
<Root>
    <Person>
        <Name>John Doe</Name>
        <Age>30</Age>
        <Address>
            <Street>Main St</Street>
            <City>New York</City>
        </Address>
    </Person>
</Root>
```

🔹 **Tree Structure ในโปรแกรม:**
```
Root  
└── Person  
    ├── Name  
    ├── Age  
    └── Address  
        ├── Street  
        └── City  
```

---

## 💻 ความต้องการของระบบ
- **Java Runtime Environment (JRE) 17+**
- ระบบปฏิบัติการ: Windows, macOS, หรือ Linux

---

© 2025 **Phumsith Insakul**. All rights reserved.

💬 **แจ้งปัญหาหรือเสนอแนะ:** [GitHub Issues](https://github.com/username/DynamicXMLTreeEditor/issues) 🚀

