
# react-native-sunmi-v2-printer

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE) 

## SUNMI V2 Printer for React-Native

This project is forked from [kev-ac/react-native-sunmi-v2-printer](https://github.com/kev-ac/react-native-sunmi-v2-printer).

Offical Demos refer: https://github.com/shangmisunmi/SunmiPrinterDemo

## Installation

**Step 1.**

Install from source

```bash
npm install https://github.com/syumai/react-native-sunmi-v2-printer.git --save
yarn add https://github.com/syumai/react-native-sunmi-v2-printer.git --save
```

**Step 2:** (For React-Native < 0.60)

Links this plugin to your project.

```bash
react-native link react-native-sunmi-v2-printer
```

or you may need to link manually

* modify settings.gradle

```javascript
include ':react-native-sunmi-v2-printer'
project(':react-native-sunmi-v2-printer').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-sunmi-v2-printer/android')
```

* modify  app/build.gradle,add dependenceie：

```javascript
compile project(':react-native-sunmi-v2-printer')
```

* adds package references to  MainPackage.java

```java
import com.sunmi.v2.printer.SunmiV2PrinterPackage;
...

 @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new SunmiV2PrinterPackage()
      );
    }
```

**Step 3:**

Import in React-Native:

```javascript
import SunmiV2Printer from 'react-native-sunmi-v2-printer';
```

## API

### Constants

| Name | Type| Description |
|:-----:|:-----:|:-----------:|
| Constants | string | Printer's status |
| hasPrinter | boolean | Is printer available |
| printerVersion | string | Printer's version |
| printerSerialNo | string | Printer's serial number |
| printerModal | string | Printer's model |

### Printer Status

|  Name | Description |
|:-----:|:-----------:|
| OUT_OF_PAPER_ACTION | Printer of paper |
| ERROR_ACTION | Printing error |
| NORMAL_ACTION | Printing normal |
| COVER_OPEN_ACTION | Printer's cover has open |
| COVER_ERROR_ACTION | Printer's cover is unusal |
| KNIFE_ERROR_1_ACTION | 切刀异常1－卡切刀 |
| KNIFE_ERROR_2_ACTION | 切刀异常2－切刀修复 |
| OVER_HEATING_ACITON | Printer is overheat |
| FIRMWARE_UPDATING_ACITON | Upgrade printer's firmware |
