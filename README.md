🌐 **เลือกภาษา | Choose Language**:  
[ภาษาไทย](#readme-ภาษาไทย) | [English](#readme-in-english)

---
## README ภาษาไทย
# DynamicXMLTreeEditor

**DynamicXMLTreeEditor** เป็นโปรแกรมแก้ไขไฟล์ XML ในรูปแบบ Tree Structure ด้วย GUI ที่ใช้งานง่าย รองรับการเพิ่ม ลบ แก้ไขค่า Undo/Redo และการจัดการไฟล์ XML โดยไฟล์ `.jar` และ `.class` พร้อมสำหรับใช้งาน และเตรียมแปลงเป็น `.exe` ในขั้นถัดไป

## ฟีเจอร์หลัก
- **Tree Structure:** แสดงผลโครงสร้างไฟล์ XML เป็นรูปแบบ Tree  
- **เพิ่ม/ลบ/แก้ไขฟิลด์:** ปรับแต่ง Field Name และ Value ของ XML ได้ง่าย  
- **Undo/Redo:** รองรับการย้อนกลับหรือกู้คืนการเปลี่ยนแปลง  
- **การจัดการไฟล์:** โหลดไฟล์ XML และบันทึกไฟล์ที่แก้ไข  
- **GUI ที่ใช้งานง่าย:** ออกแบบด้วย Swing พร้อมปุ่มฟังก์ชันที่ครบถ้วน  

## วิธีการใช้งาน
1. **รันโปรแกรม:**  
   - รันไฟล์ `.jar`: `java -jar DynamicXMLTreeEditor.jar`  
   - หรือรันไฟล์ `.class`:  
     ```bash
     javac DynamicXMLTreeEditor.java  
     java DynamicXMLTreeEditor  
     ```  
2. **ฟังก์ชันในโปรแกรม:**  
   - **Load XML:** คลิกปุ่ม `Load XML` เพื่อเลือกไฟล์ XML  
   - **เพิ่มฟิลด์:** ใส่ Field Name และ Value แล้วคลิก `Add Subfield`  
   - **ลบฟิลด์:** เลือกฟิลด์ที่ต้องการลบ แล้วคลิก `Delete Field`  
   - **Undo/Redo:** ใช้ปุ่ม `Undo` หรือ `Redo` เพื่อย้อนกลับหรือกู้คืน  
   - **บันทึกไฟล์:** คลิก `Save File` เพื่อบันทึกไฟล์ XML  

## ตัวอย่าง XML ที่รองรับ
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

Tree ที่แสดงในโปรแกรม:  
```
Root  
└── Person  
    ├── Name  
    ├── Age  
    └── Address  
        ├── Street  
        └── City  
```

## แผนการพัฒนา 
- **แปลงจาก WSDL เป็น XML:** เพิ่มฟีเจอร์การแปลงไฟล์ WSDL เป็น XML โดยตรงภายในแอป เพื่อให้ผู้ใช้ไม่ต้องใช้เครื่องมือแปลงภายนอก
- แปลงโปรแกรมเป็น `.exe` ด้วย Launch4j  
- เพิ่มการรองรับ XML Attributes  
- ปรับปรุง UI ให้สวยงามและยืดหยุ่นมากขึ้น  
- รองรับโครงสร้าง XML ขนาดใหญ่และซับซ้อน

## ความต้องการระบบ
- **Java Runtime Environment (JRE)** เวอร์ชัน 8 ขึ้นไป  
- ระบบปฏิบัติการ: Windows, macOS, หรือ Linux  

---

**หมายเหตุ:** หากพบปัญหาหรือมีข้อเสนอแนะ ติดต่อผ่าน [GitHub Issues](https://github.com/username/DynamicXMLTreeEditor/issues)  

---
## README in English
# DynamicXMLTreeEditor

**DynamicXMLTreeEditor** is a GUI-based XML file editor with a Tree Structure format, providing an easy-to-use interface for adding, deleting, editing values, and supporting Undo/Redo functionality. The program is packaged as `.jar` and `.class` files ready for use, with plans to convert it into an `.exe` file in the next phase.

## Key Features
- **Tree Structure:** Displays the XML file structure in a Tree format.  
- **Add/Delete/Edit Fields:** Easily customize Field Names and Values within the XML.  
- **Undo/Redo:** Supports undoing or redoing changes.  
- **File Management:** Load XML files and save modified files.  
- **User-friendly GUI:** Designed with Swing, providing complete functional buttons.  

## How to Use
1. **Run the Program:**  
   - Run the `.jar` file: `java -jar DynamicXMLTreeEditor.jar`  
   - Or run the `.class` file:  
     ```bash
     javac DynamicXMLTreeEditor.java  
     java DynamicXMLTreeEditor  
     ```  
2. **Program Functions:**  
   - **Load XML:** Click the `Load XML` button to select an XML file.  
   - **Add Fields:** Enter Field Name and Value, then click `Add Subfield`.  
   - **Delete Fields:** Select the field to delete and click `Delete Field`.  
   - **Undo/Redo:** Use the `Undo` or `Redo` buttons to undo or restore changes.  
   - **Save File:** Click `Save File` to save the modified XML file.  

## Example of Supported XML
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

Tree displayed in the program:  
```
Root  
└── Person  
    ├── Name  
    ├── Age  
    └── Address  
        ├── Street  
        └── City  
```

## Development Plan
- **WSDL to XML Conversion:** Add a feature to directly convert WSDL files to XML within the app, eliminating the need for external conversion tools.  
- Convert the program to `.exe` format using Launch4j.  
- Add support for XML Attributes.  
- Enhance UI to be more aesthetic and flexible.  
- Support larger and more complex XML structures.  

## System Requirements
- **Java Runtime Environment (JRE)** version 8 or above.  
- Operating System: Windows, macOS, or Linux.  

---

**Note:** If you encounter issues or have suggestions, contact us through [GitHub Issues](https://github.com/username/DynamicXMLTreeEditor/issues)  

---
