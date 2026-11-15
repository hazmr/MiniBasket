import type { ComponentProps } from "react";
import type { FormAction } from "@/lib/action-state";
import { ActionForm } from "./action-form";
import { SubmitButton } from "./submit-button";

type Props = ComponentProps<typeof SubmitButton> & { action: FormAction };

export function ActionButton({ action, variant = "outline", ...buttonProps }: Props) {
  return (
    <ActionForm action={action} errorsAs="toast" className="contents">
      <SubmitButton variant={variant} {...buttonProps} />
    </ActionForm>
  );
}
