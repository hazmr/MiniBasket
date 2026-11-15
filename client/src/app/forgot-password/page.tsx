import Link from "next/link";
import { forgotPassword } from "@/actions/auth";
import { ActionForm } from "@/components/action-form";
import { AuthCard } from "@/components/auth-card";
import { SubmitButton } from "@/components/submit-button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";

export default function ForgotPasswordPage() {
  return (
    <AuthCard
      title="Forgot your password?"
      description="We'll send you a token to choose a new one."
      footer={<Link href="/reset-password" className="font-medium text-primary hover:underline">I already have a reset code</Link>}
    >
      <ActionForm action={forgotPassword} className="flex flex-col gap-5">
        <Field>
          <FieldLabel htmlFor="email">Email</FieldLabel>
          <Input id="email" type="email" name="email" required autoComplete="email" />
        </Field>
        <SubmitButton size="lg" className="w-full">Send reset token</SubmitButton>
      </ActionForm>
    </AuthCard>
  );
}
