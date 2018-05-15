  CREATE TABLE "TEXTS" 
   (	"ID" NUMBER, 
	"DESCR" CLOB, 
	"NAME" VARCHAR2(20 BYTE)
   ) ;


Insert into TEXTS (ID,NAME,DESCR) values (1,'simple','This is some short description');
Insert into TEXTS (ID,NAME,DESCR) values (2,'longer simple','Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus fringilla posuere urna vel elementum. Sed mi tortor, mattis euismod velit vel, congue condimentum nisi. Phasellus mi tortor, ornare sit amet semper sed, finibus non risus. Proin volutpat nulla ut ante porttitor faucibus. Quisque tempus neque quis arcu semper dictum. Nam sed enim porttitor, cursus erat id, commodo odio. Donec venenatis rutrum tellus et laoreet. Quisque mi ex, maximus eget luctus in, sollicitudin sed velit. Praesent et ultrices leo, vel consectetur elit. Integer nec metus a orci fringilla tempus. Maecenas congue metus vitae erat fermentum ullamcorper. Curabitur pharetra blandit elit, eget suscipit ligula scelerisque vitae. Phasellus tortor lorem, tempus vitae quam ac, convallis maximus dui.');
Insert into TEXTS (ID,NAME,DESCR) values (3,'long, few paragraphs','Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus fringilla posuere urna vel elementum. Sed mi tortor, mattis euismod velit vel, congue condimentum nisi. Phasellus mi tortor, ornare sit amet semper sed, finibus non risus. Proin volutpat nulla ut ante porttitor faucibus. Quisque tempus neque quis arcu semper dictum. Nam sed enim porttitor, cursus erat id, commodo odio. Donec venenatis rutrum tellus et laoreet. Quisque mi ex, maximus eget luctus in, sollicitudin sed velit. Praesent et ultrices leo, vel consectetur elit. Integer nec metus a orci fringilla tempus. Maecenas congue metus vitae erat fermentum ullamcorper. Curabitur pharetra blandit elit, eget suscipit ligula scelerisque vitae. Phasellus tortor lorem, tempus vitae quam ac, convallis maximus dui.' || chr(10) || chr(10) || chr(10) || 'Proin ac justo neque. Praesent non semper lacus. Ut semper consectetur leo nec gravida. Fusce at placerat turpis. Praesent congue vehicula venenatis. Mauris ultrices, sem ut vestibulum laoreet, lectus diam ultrices ipsum, sit amet interdum leo enim at erat. In consectetur libero eu mollis consectetur. Nunc aliquam tristique dui, a dignissim velit porta ac. Duis fringilla ante nec imperdiet fringilla. Mauris accumsan volutpat aliquet. Pellentesque fermentum placerat dui. Suspendisse id leo purus. Suspendisse aliquam ipsum in vehicula condimentum. Vivamus massa tortor, finibus ac sodales eget, dictum et urna. Suspendisse consectetur purus non ipsum pretium mollis.' || chr(10) || chr(10) || chr(10) || 'Nunc ornare auctor semper. Maecenas ultricies et lorem sit amet ultricies. Ut ornare diam nisl. Donec finibus lobortis velit, ultrices eleifend est. Suspendisse potenti. Sed finibus aliquet enim vitae iaculis. Ut quis enim ut nisl tristique ullamcorper. Praesent volutpat leo sed molestie molestie. Pellentesque nec massa pulvinar, tristique dui eu, eleifend ex. Donec convallis mi erat, eu sodales odio interdum sed. Sed sollicitudin bibendum est eget pulvinar. Ut sed tristique ante, nec facilisis felis. Aenean in ornare ligula, at rutrum enim. Mauris rhoncus id velit vitae blandit. In id aliquet justo.');
Insert into TEXTS (ID,NAME,DESCR) values (4,'xml','<widget>
    <debug>on</debug>
    <window title="Sample Konfabulator Widget">
        <name>main_window</name>
        <width>500</width>
        <height>500</height>
    </window>
    <image src="Images/Sun.png" name="sun1">
        <hOffset>250</hOffset>
        <vOffset>250</vOffset>
        <alignment>center</alignment>
    </image>
    <text data="Click Here" size="36" style="bold">
        <name>text1</name>
        <hOffset>250</hOffset>
        <vOffset>100</vOffset>
        <alignment>center</alignment>
        <onMouseUp>
            sun1.opacity = (sun1.opacity / 100) * 90
        </onMouseUp>
    </text>
</widget>');
Insert into TEXTS (ID,NAME,DESCR) values (5,'json','{"menu": {
    "header": "SVG Viewer",
    "items": [
        {"id": "Open"},
        {"id": "OpenNew", "label": "Open New"},
        null,
        {"id": "ZoomIn", "label": "Zoom In"},
        {"id": "ZoomOut", "label": "Zoom Out"},
        {"id": "OriginalView", "label": "Original View"},
        null,
        {"id": "Quality"},
        {"id": "Pause"},
        {"id": "Mute"},
        null,
        {"id": "Find", "label": "Find..."},
        {"id": "FindAgain", "label": "Find Again"},
        {"id": "Copy"},
        {"id": "CopyAgain", "label": "Copy Again"},
        {"id": "CopySVG", "label": "Copy SVG"},
        {"id": "ViewSVG", "label": "View SVG"},
        {"id": "ViewSource", "label": "View Source"},
        {"id": "SaveAs", "label": "Save As"},
        null,
        {"id": "Help"},
        {"id": "About", "label": "About Adobe CVG Viewer..."}
    ]
}}');
Insert into TEXTS (ID,NAME,DESCR) values (6,'html','<table width="100%" border="0">');
