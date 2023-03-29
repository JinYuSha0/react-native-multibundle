import React, {memo} from "react";
// @ts-ignore
import TextInput from "./TextInput";
import { TextInputProps } from "react-native";

export interface CustomTextInputProps extends TextInputProps {
  onlyNumber?: boolean;
  thousandth?: boolean;
  decimal?: boolean;
  separator?: string;
}

function extraProps(props: CustomTextInputProps) {
  const { onlyNumber, thousandth, separator } = props;
  const extra: Record<string, any> = {};
  if (onlyNumber) {
    extra.type = 1;
  }
  if (thousandth) {
    extra.type = 2;
    if (!separator) {
      extra.thousands = ",";
    } else {
      extra.thousands = separator;
    }
  }
  return extra;
}

const TextInputMemo = memo(TextInput)

export const CustomTextInput = memo(React.forwardRef<TextInputProps, TextInput>(
  (props, ref) => {
    const {type, thousands} = extraProps(props);
    return <TextInputMemo ref={ref} {...props} type={type} thousands={thousands}/>;
  }
));
